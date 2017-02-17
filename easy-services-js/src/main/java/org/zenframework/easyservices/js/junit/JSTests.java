package org.zenframework.easyservices.js.junit;

import java.lang.annotation.RetentionPolicy;

import java.lang.annotation.Retention;

@Retention(RetentionPolicy.RUNTIME)
public @interface JSTests {

    String[] value();

}