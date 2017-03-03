package com.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) //声明类或者接口
@Retention(RetentionPolicy.SOURCE)//注解信息声明有效时间，编译时被抛弃
public @interface ApiFactory {

}
