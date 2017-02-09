package org.zenframework.easyservices.test.dynamic;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.test.simple.Addition;
import org.zenframework.easyservices.test.simple.Function;
import org.zenframework.easyservices.test.simple.Substraction;

public class DynamicTest extends AbstractServiceTest {

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

    public void testDynamic() throws Exception {
        Calculator calc = getClient(Calculator.class, "/calc");
        Function add = calc.getFunction("add");
        Function sub = calc.getFunction("sub");
        assertEquals(3, calc.call(add, 1, 2));
        assertEquals(1, calc.call(sub, 3, 2));
    }

    public void testStreams() throws Exception {
        StreamFactory streams = getClient(StreamFactory.class, "/streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOuptputStream();
        try {
            byte[] buf = new byte[100];
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                out.write(buf, 0, n);
        } finally {
            IOUtils.closeQuietly(in, out);
        }
    }

}
