package org.zenframework.easyservices.js.junit;

public class JSTest implements Runnable {

    private final String name;
    private final Runnable test;

    public JSTest(String name, Runnable test) {
        this.name = name;
        this.test = test;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        test.run();
    }

}
