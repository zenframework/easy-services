package org.zenframework.easyservices.test.simple;

public class Addition implements Function {

    @Override
    public int call(int a, int b) {
        return a + b;
    }

}
