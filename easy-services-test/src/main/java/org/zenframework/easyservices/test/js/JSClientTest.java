package org.zenframework.easyservices.test.js;

import org.junit.Test;
import org.zenframework.easyservices.js.client.JSClientGenerator;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.test.simple.Function;

public class JSClientTest extends AbstractServiceTest {

    @Test
    public void testJSClient() throws Exception {
        JSClientGenerator generator = new JSClientGenerator();
        generator.generateJSClient(System.out, Function.class);
    }

}
