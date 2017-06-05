package com.github.saadfarooq.khoji;

import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;

public class KhojiCollection {
    private final List<TypeMirror> mainDeps;
    private final Map<TypeElement, List<TypeMirror>> classDepsMap;
    private final TypeMirror iface;
    private final String genPkgName;
    private String genClassName;
    private Types types;
    private boolean fileWritten = false;

    public KhojiCollection(TypeMirror iface, Elements elements, Types types) {
        this.types = types;
        mainDeps = new ArrayList<>();
        classDepsMap = new LinkedHashMap<>();
        this.iface = iface;
        this.genClassName = types.asElement(iface).getSimpleName() + "Collection";
        this.genPkgName = elements.getPackageOf(types.asElement(iface)).getQualifiedName().toString();
    }

    public void addMainDependency(TypeMirror typeMirror) {
        boolean exists = false;
        for (TypeMirror type : mainDeps) {
            if (types.isSameType(type, typeMirror)) {
                exists = true;
            }
        }
        if (!exists) {
            mainDeps.add(typeMirror);
        }
    }

    public void addClassDependencies(TypeElement clazz, List<TypeMirror> classDeps) {
        classDepsMap.put(clazz, classDeps);
    }

    public void createFile(Filer filer) throws IOException {
        TypeSpec.Builder magicItems = TypeSpec.classBuilder(genClassName).addModifiers(Modifier.PUBLIC);
        // Create constructor with main dependencies and field for each
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        for (TypeMirror dependency : mainDeps) {
            String fieldName = getVariableName(dependency);
            constructorBuilder.addParameter(TypeName.get(dependency), fieldName);
            constructorBuilder.addStatement("this.$N = $N", fieldName, fieldName);
            magicItems.addField(TypeName.get(dependency), fieldName, Modifier.PRIVATE, Modifier.FINAL);
        }

        ParameterizedTypeName listOfInterface = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(iface)); // List<Interface>
        constructorBuilder.addStatement("this.$N = initItems()", "khojiItems");
        magicItems.addField(listOfInterface, "khojiItems", Modifier.PRIVATE, Modifier.FINAL);
        magicItems.addMethod(constructorBuilder.build());

        MethodSpec.Builder khojiItemsMethodBuilder = MethodSpec.methodBuilder("getCollectedItems")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(listOfInterface);
        khojiItemsMethodBuilder.addStatement("return $N", "khojiItems");

        magicItems.addMethod(khojiItemsMethodBuilder.build());


        MethodSpec.Builder initDrawerItemsList = MethodSpec.methodBuilder("initItems")
                .addModifiers(Modifier.PRIVATE)
                .returns(listOfInterface)
                .addStatement("$T result = new $T<>()", listOfInterface, ClassName.get(ArrayList.class));

        for (Map.Entry<TypeElement, List<TypeMirror>> classEntry : classDepsMap.entrySet()) {
            StringBuilder statementFormat = new StringBuilder("result.add(new $T( ");
            for (TypeMirror dep : classEntry.getValue()) {
                statementFormat.append(getVariableName(dep));
                statementFormat.append(",");
            }
            statementFormat.deleteCharAt(statementFormat.length() - 1)
                    .append("))");
            initDrawerItemsList.addStatement(statementFormat.toString(), classEntry.getKey());
        }

        initDrawerItemsList.addStatement("return result");
        magicItems.addMethod(initDrawerItemsList.build());

        JavaFile javaFile = JavaFile.builder(genPkgName, magicItems.build()).build();
        javaFile.writeTo(filer);
        fileWritten = true;
    }

    private String getVariableName(TypeMirror type) {
        DeclaredType declaredType = (DeclaredType) type;
        StringBuilder name = new StringBuilder();
        name.append(declaredType.asElement().getSimpleName().toString().toLowerCase());
        if (declaredType.getTypeArguments().size() > 0) {
            name.append(types.asElement(declaredType.getTypeArguments().get(0)).getSimpleName().toString());
        }
        return name.toString();
    }

    public boolean isFileWritten() {
        return fileWritten;
    }
}
