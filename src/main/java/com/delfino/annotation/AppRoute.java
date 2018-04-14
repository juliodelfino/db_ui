package com.delfino.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import spark.route.HttpMethod;

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.FIELD)  
public @interface AppRoute {

	boolean skipAuthentication() default false;
	HttpMethod httpMethod() default HttpMethod.get;
	boolean requireAdmin() default false;

}
