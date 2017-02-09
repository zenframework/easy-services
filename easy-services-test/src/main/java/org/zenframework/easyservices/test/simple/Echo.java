package org.zenframework.easyservices.test.simple;

public interface Echo {

    Class<?> echo(Class<?> cls);

    void throwException(Exception e) throws Exception;

    int nextInteger();

    void doNothing();

}
