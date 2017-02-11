package org.zenframework.easyservices.test.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.test.simple.Addition;
import org.zenframework.easyservices.test.simple.Function;
import org.zenframework.easyservices.test.simple.Substraction;

public class DynamicTest extends AbstractServiceTest {

    public DynamicTest(String format) {
        super(format);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CalculatorImpl functions = new CalculatorImpl();
        functions.getFunctions().put("add", new Addition());
        functions.getFunctions().put("sub", new Substraction());
        getServiceRegistry().bind("/calc", functions);
        getServiceRegistry().bind("/streams", new StreamFactoryImpl(1024 * 1024));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("/calc");
        getServiceRegistry().unbind("/streams");
    }

    @Test
    public void testDynamic() throws Exception {
        Calculator calc = getClient(Calculator.class, "/calc");
        Function add = calc.getFunction("add");
        Function sub = calc.getFunction("sub");
        assertEquals(3, calc.call(add, 1, 2));
        assertEquals(1, calc.call(sub, 3, 2));
    }

    @Test
    public void testRemoteStreams() throws Exception {
        StreamFactory streams = getClient(StreamFactory.class, "/streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOuptputStream();
        copy(in, out);
    }

    @Test
    public void testLocalStreams() throws Exception {
        StreamFactory streams = (StreamFactory) getServiceRegistry().lookup("/streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOuptputStream();
        copy(in, out);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in, out);
        }
    }

}
