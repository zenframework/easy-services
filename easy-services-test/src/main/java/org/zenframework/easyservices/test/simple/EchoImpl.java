package org.zenframework.easyservices.test.simple;

import java.util.concurrent.atomic.AtomicInteger;

public class EchoImpl implements Echo {

    private AtomicInteger n = new AtomicInteger();

    @Override
    public Class<?> echo(Class<?> cls) {
        return cls;
    }

    @Override
    public void throwException(Exception e) throws Exception {
        throw e;
    }

    @Override
    public int nextInteger() {
        return n.getAndIncrement();
    }

    @Override
    public void doNothing() {}

}
