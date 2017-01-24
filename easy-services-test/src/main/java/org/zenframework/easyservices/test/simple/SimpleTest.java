package org.zenframework.easyservices.test.simple;

import org.zenframework.easyservices.test.AbstractServiceTest;

public class SimpleTest extends AbstractServiceTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getServiceRegistry().bind("/add", new Addition());
        getServiceRegistry().bind("/sub", new Substraction());
    }

    @Override
    public void tearDown() throws Exception {
        getServiceRegistry().unbind("/add");
        getServiceRegistry().unbind("/sub");
        super.tearDown();
    }

    public void testSimpleServices() throws Exception {
        assertEquals(3, getClient(Function.class, "/add").call(1, 2));
        assertEquals(1, getClient(Function.class, "/sub").call(3, 2));
    }

}
