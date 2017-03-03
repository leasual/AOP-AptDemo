package com.example;

import javax.annotation.processing.RoundEnvironment;

public interface IProcessor {

    void process(RoundEnvironment roundEnv, AnnotationProcessor annotationProcessor);
}
