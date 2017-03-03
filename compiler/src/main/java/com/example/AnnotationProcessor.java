package com.example;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by james.li on 2017/3/3.
 */

@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({//标注注解处理器支持的注解类型
        "com.example.ApiFactory"
})
public class AnnotationProcessor extends AbstractProcessor {
    public Filer mFiler;
    public Elements mElements;
    public Messager mMessager;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();

        new ApiFactoryProcessor().process(roundEnv, this);
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
