package org.zenframework.easyservices.test.js;

import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.js.junit.JSTestSuite;
import org.zenframework.easyservices.js.junit.JSTests;
import org.zenframework.easyservices.test.HttpServer;
import org.zenframework.easyservices.test.TestContext;
import org.zenframework.easyservices.test.simple.Addition;

@JSTests(value = { "classpath://export/generic/api/ProxyFactoryTest.js" })
public class ProxyFactoryTest extends JSTestSuite {

    private final HttpServer server = new HttpServer(TestContext.JETTY_PORT);

    @Override
    protected void setUp() {
        super.setUp();
        try {
            JNDIHelper.getDefaultContext().bind("/add", new Addition());
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void tearDown() {
        super.tearDown();
        try {
            JNDIHelper.getDefaultContext().unbind("/add");
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
