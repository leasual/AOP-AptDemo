package com.example;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Created by james.li on 2017/3/3.
 */

public class ApiFactoryProcessor implements IProcessor {

    @Override
    public void process(RoundEnvironment roundEnv, AnnotationProcessor annotationProcessor) {
        try {
            String className = "HttpMethods";
            TypeSpec.Builder apiFactoryClass = TypeSpec.classBuilder(className)
                    .addJavadoc("HttpMethods 此类由apt自动生成\n")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            //ElementFilter.typesIn 返回 elements 中字段的集合。 roundEnv.getElementsAnnotatedWith获取被@Factory注解的元素(Element可以是类，方法，变量等)列表
            for (TypeElement element : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(ApiFactory.class))) {
                annotationProcessor.mMessager.printMessage(Diagnostic.Kind.NOTE, "正在解析所有被ApiFactory注解的元素 " + element.toString());
                //返回类或者接口中所有的变量、方法、构造方法和成员类型
                for (Element e: element.getEnclosedElements()) {
                    annotationProcessor.mMessager.printMessage(Diagnostic.Kind.NOTE, "正在解析类或方法当中的所有元素 "+ e.toString());
                    //获取方法
                    ExecutableElement executableElement = (ExecutableElement) e;
                    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(e.getSimpleName().toString())
                            .addJavadoc(element.getSimpleName().toString()+ " " + e.getSimpleName().toString() + "由apt自动生成\n")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

                    //方法的返回类型
                    methodBuilder.returns(TypeName.get(executableElement.getReturnType()));
                    //获取所有参数
                    String params = " ";
                    for (VariableElement ve: executableElement.getParameters()) {
                        //往方法内添加参数
                        methodBuilder.addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString());
                        params += ve.getSimpleName().toString() + ",";
                        annotationProcessor.mMessager.printMessage(Diagnostic.Kind.NOTE, "param= " + params);
                    }
                    methodBuilder.addStatement(
                            "return $T.getInstance()" +
                                    ".createService($T.class).$L($L)\n" +
                                    ".subscribeOn($T.io())\n" +
                                    ".observeOn($T.mainThread())",
                            ClassName.get("com.wesoft.swaggerdemo.network", "HttpService"),
                            element,
                            e.getSimpleName().toString(),
                            params.equals("")? "" : params.substring(0, params.length() - 1),
                            ClassName.get("rx.schedulers", "Schedulers"),
                            ClassName.get("rx.android.schedulers", "AndroidSchedulers")
                    );
                    apiFactoryClass.addMethod(methodBuilder.build());
                }
            }

            JavaFile file = JavaFile.builder("com.apt", apiFactoryClass.build()).build();
            file.writeTo(annotationProcessor.mFiler);
        } catch (FilerException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
