package org.zenframework.easyservices.test.simple;

public class Addition implements Function {

    @Override
    public int call(int a, int b) {
        return a + b;
    }

    @Override
    public void close() {
        // do nothing, "closing" function is done by ServiceInvoker
    }

}
