package com.yunmei.frame.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
	String name();
	enum TYPE{MENU,OPER,DATA} ;
	TYPE type() default TYPE.OPER;
	String fields() default "";
	int order() default 1;
}
