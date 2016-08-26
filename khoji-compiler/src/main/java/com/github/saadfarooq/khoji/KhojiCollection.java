package com.github.saadfarooq.khoji;

import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class KhojiCollection {
    private final Set<TypeMirror> mainDeps;
    private final Map<TypeElement, List<TypeMirror>> classDepsMap;
    private final TypeMirror iface;
    private final String genPkgName;
    private String genClassName;
    private Types types;
    private boolean fileWritten = false;

    public KhojiCollection(TypeMirror iface, Elements elements, Types types) {
        this.types = types;
        mainDeps = new HashSet<>();
        classDepsMap = new HashMap<>();
        this.iface = iface;
        this.genClassName = types.asElement(iface).getSimpleName() + "Collection";
        this.genPkgName = elements.getPackageOf(types.asElement(iface)).getQualifiedName().toString();
    }

    public void addMainDependency(TypeMirror typeMirror) {
        mainDeps.add(typeMirror);
    }

    public void addClassDependencies(TypeElement clazz, List<TypeMirror> classDeps) {
        classDepsMap.put(clazz, classDeps);
    }

    public void createFile(Filer filer) throws IOException {
        TypeSpec.Builder magicDrawerItems = TypeSpec.classBuilder(genClassName).addModifiers(Modifier.PUBLIC);
        // Create constructor with main dependencies and field for each
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        for (TypeMirror dependency : mainDeps) {
            String fieldName = types.asElement(dependency).getSimpleName().toString().toLowerCase();
            constructorBuilder.addParameter(TypeName.get(dependency), fieldName);
            constructorBuilder.addStatement("this.$N = $N", fieldName, fieldName);
            magicDrawerItems.addField(TypeName.get(dependency), fieldName, Modifier.PRIVATE, Modifier.FINAL);
        }

        ParameterizedTypeName listOfInterface = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(iface)); // List<Interface>
        constructorBuilder.addStatement("this.$N = initDrawerItemsList()", "khojiItems");
        magicDrawerItems.addField(listOfInterface, "khojiItems", Modifier.PRIVATE, Modifier.FINAL);
        magicDrawerItems.addMethod(constructorBuilder.build());

        MethodSpec.Builder khojiItemsMethodBuilder = MethodSpec.methodBuilder("getCollectedItems")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(listOfInterface);
        khojiItemsMethodBuilder.addStatement("return $N", "khojiItems");

        magicDrawerItems.addMethod(khojiItemsMethodBuilder.build());


        MethodSpec.Builder initDrawerItemsList = MethodSpec.methodBuilder("initDrawerItemsList")
                .addModifiers(Modifier.PRIVATE)
                .returns(listOfInterface)
                .addStatement("$T result = new $T<>()", listOfInterface, ClassName.get(ArrayList.class));

        for (Map.Entry<TypeElement, List<TypeMirror>> classEntry : classDepsMap.entrySet()) {
            StringBuilder statementFormat = new StringBuilder("result.add(new $T( ");
            for (TypeMirror dep : classEntry.getValue()) {
                statementFormat.append(types.asElement(dep).getSimpleName().toString().toLowerCase());
                statementFormat.append(",");
            }
            statementFormat.deleteCharAt(statementFormat.length() - 1)
                    .append("))");
            initDrawerItemsList.addStatement(statementFormat.toString(), classEntry.getKey());
        }

        initDrawerItemsList.addStatement("return result");
        magicDrawerItems.addMethod(initDrawerItemsList.build());

        JavaFile javaFile = JavaFile.builder(genPkgName, magicDrawerItems.build()).build();
        javaFile.writeTo(filer);
        fileWritten = true;
    }

    public boolean isFileWritten() {
        return fileWritten;
    }
}
