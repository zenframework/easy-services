package org.zenframework.easyservices.test.simple;

public class EchoImpl implements Echo {

    @Override
    public Class<?> echo(Class<?> cls) {
        return cls;
    }

    @Override
    public void throwException(Exception e) throws Exception {
        throw e;
    }

}
