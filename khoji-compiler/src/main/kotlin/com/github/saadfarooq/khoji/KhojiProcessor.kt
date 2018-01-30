package com.github.saadfarooq.khoji

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@AutoService(Processor::class)
class KhojiProcessor : AbstractProcessor() {
    lateinit var typeUtils: Types
    lateinit var elementUtils: Elements
    lateinit var filer: Filer
    lateinit var messager: Messager

    override fun getSupportedAnnotationTypes(): Set<String> {
        return hashSetOf(KhojiTarget::class.java.canonicalName,
            KhojiAlwaysGenerate::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        typeUtils = processingEnv.typeUtils
        elementUtils = processingEnv.elementUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    data class KhojiAnnotatedClassDetails(val annotatedClass: TypeElement?,
                                          val khojiInterface: TypeMirror,
                                          val constructorParameters: List<TypeMirror>)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(KhojiTarget::class.java)
            .onEach {
                if (it.kind != ElementKind.CLASS) {
                    error(it, "${KhojiTarget::class.java.simpleName} annotations can only be applied to classes!")
                    return false
                }
            }
            .filterIsInstance(TypeElement::class.java) // cast while filtering
            .onEach {
                if (it.interfaces.size != 1) {
                    error(it, "@${KhojiTarget::class.java.simpleName} annotated classes must implement one and only one interface. Sorry!!")
                    return false
                }
            }
            .sortedWith(compareBy({ it.toString() })) // sort the annotated class by name for deterministic order
            .map { KhojiAnnotatedClassDetails(it, it.interfaces[0], constructorParametersForClass(it)) }
            .groupBy { it.khojiInterface }
            .apply {
                // find any KhojiAlwaysGenerate interfaces that aren't in grouped map
                roundEnv.getElementsAnnotatedWith(KhojiAlwaysGenerate::class.java)
                    .onEach {
                        if (it.kind != ElementKind.INTERFACE) {
                            error(it, "${KhojiAlwaysGenerate::class.java.simpleName} annotations" +
                                "can only be applied to interfaces!")
                            return false
                        }
                    }
                    .filterIsInstance(TypeElement::class.java)
                    .filterNot { containsKey(it.asType()) }
                    .forEach {
                        generateFiles(
                            it.asType(),
                            listOf(
                                KhojiAnnotatedClassDetails(
                                    null,
                                    it.asType(),
                                    constructorParametersFromAnnotation(it)
                                )
                            )
                        )
                    }
            }
            .forEach { iface, list -> generateFiles(iface, list) }
        return true
    }

    private fun generateFiles(iface: TypeMirror, list: List<KhojiAnnotatedClassDetails> = emptyList()) {
        typeUtils.asElement(iface).let {
            JavaFile.builder(
                elementUtils.getPackageOf(it).qualifiedName.toString(),
                TypeSpec.classBuilder("${it.simpleName}Collection")
                    .addModifiers(Modifier.PUBLIC)
                    .addFields(
                        list.map { it.constructorParameters }
                            .flatten()
                            .distinctBy { it.variableName() } // no equality on TypeMirror
                            .map {
                                FieldSpec.builder(TypeName.get(it), it.variableName())
                                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                                    .build()
                            }
                    )
                    .addField(iface.parametrizedReturnType(), "khojiItems",
                        Modifier.PRIVATE, Modifier.FINAL)
                    .addMethod(
                        MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameters(
                                list.map { it.constructorParameters }
                                    .flatten()
                                    .distinctBy { it.variableName() }
                                    .map {
                                        ParameterSpec.builder(
                                            TypeName.get(it),
                                            it.variableName())
                                            .build()
                                    }

                            )
                            .apply {
                                list.map { it.constructorParameters }
                                    .flatten()
                                    .distinctBy { it.variableName() }
                                    .forEach {
                                        this.addStatement("this.\$N = \$N",
                                            it.variableName(),
                                            it.variableName())
                                    }
                            }
                            .addStatement("this.\$N = initItems()", "khojiItems")
                            .build()
                    )
                    .addMethod(
                        MethodSpec.methodBuilder("getCollectedItems")
                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                            .returns(iface.parametrizedReturnType())
                            .addStatement("return \$N", "khojiItems")
                            .build()
                    )
                    .addMethod(
                        MethodSpec.methodBuilder("initItems")
                            .addModifiers(Modifier.PRIVATE)
                            .returns(iface.parametrizedReturnType())
                            .addStatement("\$T result = new \$T<>()",
                                iface.parametrizedReturnType(),
                                ClassName.get(ArrayList::class.java))
                            .apply {
                                list.forEach { (annotatedClass, _, constructorParameters) ->
                                    annotatedClass?.let { // only add statements if class in annotated
                                        if (constructorParameters.isEmpty()) {
                                            this.addStatement("result.add(new \$T())", annotatedClass)
                                        } else {
                                            constructorParameters.map { it.variableName() }
                                                .reduce { acc, s -> "$acc, $s" } // format variables with ,
                                                .let {
                                                    this.addStatement("result.add(new \$T($it))", annotatedClass)
                                                }
                                        }
                                    }
                                }
                            }
                            .addStatement("return result")
                            .build()
                    )
                    .build())
                .build()
                .writeTo(filer)
        }
    }

    private fun constructorParametersForClass(khojiTargetAnnotatedClass: TypeElement): List<TypeMirror> {
        return khojiTargetAnnotatedClass.enclosedElements
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .filterIsInstance(ExecutableElement::class.java)
            .map { it.parameters }
            .flatten()
            .distinct()
            .map { it.asType() }
    }

    private fun constructorParametersFromAnnotation(typeElement: TypeElement): List<TypeMirror> {
        try {
            typeElement.getAnnotation(KhojiAlwaysGenerate::class.java).parameters
        } catch (e: MirroredTypesException) {
            return e.typeMirrors
        }
        return emptyList()
    }

    private fun error(e: Element, msg: String, vararg args: Any) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, *args), e)
    }

    fun TypeMirror.variableName(): String {
        return (this as DeclaredType).asElement().simpleName.toString().toLowerCase()
            .plus(
                if (this.typeArguments.isNotEmpty()) { // add parameter to name if parametrized
                    typeUtils.asElement(this.typeArguments[0]).simpleName.toString()
                } else {
                    ""
                }
            )
    }

    fun TypeMirror.parametrizedReturnType(): ParameterizedTypeName {
        return ParameterizedTypeName.get(ClassName.get(List::class.java), TypeName.get(this))
    }
}
