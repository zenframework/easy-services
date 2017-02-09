package org.zenframework.easyservices.test.simple;

import org.zenframework.easyservices.test.AbstractServiceTest;

public class SimpleTest extends AbstractServiceTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getServiceRegistry().bind("/add", new Addition());
        getServiceRegistry().bind("/sub", new Substraction());
        getServiceRegistry().bind("/echo", new EchoImpl());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("/add");
        getServiceRegistry().unbind("/sub");
        getServiceRegistry().unbind("/echo");
    }

    public void testSimpleServices() throws Exception {
        assertEquals(3, getClient(Function.class, "/add").call(1, 2));
        assertEquals(1, getClient(Function.class, "/sub").call(3, 2));
        assertEquals(Integer.class, getClient(Echo.class, "/echo").echo(Integer.class));
    }

    public void testThrowCatchException() throws Exception {
        try {
            getClient(Echo.class, "/echo").throwException(new SimpleException("exception"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e instanceof SimpleException);
            assertEquals("exception", e.getMessage());
        }
    }

    public void testSingleCall() throws Exception {
        Echo echo = getClient(Echo.class, "/echo");
        assertEquals(0, echo.nextInteger());
        assertEquals(1, echo.nextInteger());
    }

    public void testVoid() throws Exception {
        Echo echo = getClient(Echo.class, "/echo");
        echo.doNothing();
    }

}
