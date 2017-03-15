package org.zenframework.easyservices.test.simple;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.test.AbstractServiceTest;

@RunWith(Parameterized.class)
public class SimpleTest extends AbstractServiceTest {

    @Parameterized.Parameters(name = "#{index} format:{0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] { { "json" }, { "bin" } });
    }

    public SimpleTest(String format) {
        Environment.setSerializationFormat(format);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getServiceRegistry().bind("add", new Addition());
        getServiceRegistry().bind("sub", new Substraction());
        getServiceRegistry().bind("echo", new EchoImpl());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("add");
        getServiceRegistry().unbind("sub");
        getServiceRegistry().unbind("echo");
    }

    @Test
    public void testSimpleServices() throws Exception {
        assertEquals(3, getClient(Function.class, "add").call(1, 2));
        assertEquals(1, getClient(Function.class, "sub").call(3, 2));
        assertEquals(Integer.class, getClient(Echo.class, "echo").echo(Integer.class));
    }

    @Test
    public void testThrowCatchException() throws Exception {
        try {
            getClient(Echo.class, "echo").throwException(new SimpleException("exception"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getClass() + ": " + e.getMessage(), e instanceof SimpleException);
            assertEquals("exception", e.getMessage());
        }
    }

    @Test
    public void testSingleCall() throws Exception {
        Echo echo = getClient(Echo.class, "echo");
        assertEquals(0, echo.nextInteger());
        assertEquals(1, echo.nextInteger());
    }

    @Test
    public void testVoid() throws Exception {
        Echo echo = getClient(Echo.class, "echo");
        echo.doNothing();
    }

}
