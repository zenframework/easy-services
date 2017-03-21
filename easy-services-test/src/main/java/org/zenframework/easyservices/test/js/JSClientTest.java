package org.zenframework.easyservices.test.js;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.js.client.JSClientGenerator;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.test.simple.Function;

@RunWith(Parameterized.class)
public class JSClientTest extends AbstractServiceTest {

    @Parameterized.Parameters(name = "#{index} protocol/format:{0}")
    public static Collection<Object[]> params() {
        return Arrays.<Object[]> asList(arr("http/json"));
    }

    public JSClientTest(String protocolFormat) {
        super(protocolFormat);
    }

    @Test
    public void testJSClient() throws Exception {
        JSClientGenerator generator = new JSClientGenerator();
        generator.generateJSClient(System.out, Function.class);
        // TODO test JS client
    }

}
