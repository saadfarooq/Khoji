package com.github.saadfarooq.khoji;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class KhojiProcessor extends AbstractProcessor {
    public static Processor instance;
    public Types typeUtils;
    public Elements elementUtils;
    public Filer filer;
    public Messager messager;
    private Map<TypeMirror, KhojiCollection> ifaceCollectionMap;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {{
            add(KhojiTarget.class.getCanonicalName());
        }};
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        instance = this;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        ifaceCollectionMap = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(KhojiTarget.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "KhojiTarget annotations can only be applied to classes!");
                return false;
            }
            TypeElement clazz = (TypeElement) element;

            if (clazz.getInterfaces().size() != 1) {
                error(element, "@KhojiTarget annotated classes must implement one and only one interface. Sorry!!");
                return false;
            }

            TypeMirror iface = clazz.getInterfaces().get(0);
            KhojiCollection collection = getOrCreateKhojiCollection(iface);

            List<TypeMirror> classDeps = new ArrayList<>();
            for (Element e : clazz.getEnclosedElements()) {
                if (e.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement constructor = (ExecutableElement) e;
                    for (VariableElement parameter : constructor.getParameters()) {
                        collection.addMainDependency(parameter.asType());
                        classDeps.add(parameter.asType());
                    }
                }
            }
            collection.addClassDependencies(clazz, classDeps);
        }

        try {
            for (KhojiCollection khojiCollection : ifaceCollectionMap.values()) {
                if (!khojiCollection.isFileWritten()) khojiCollection.createFile(filer);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return true;
    }

    private KhojiCollection getOrCreateKhojiCollection(TypeMirror iface) {
        if (ifaceCollectionMap.get(iface) == null) {
            ifaceCollectionMap.put(iface, new KhojiCollection(iface, elementUtils, typeUtils));
        }
        return ifaceCollectionMap.get(iface);
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
