package org.zenframework.easyservices.test.js;

import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.js.junit.JSTestSuite;
import org.zenframework.easyservices.js.junit.JSTests;
import org.zenframework.easyservices.test.HttpServer;
import org.zenframework.easyservices.test.TestContext;
import org.zenframework.easyservices.test.simple.Addition;

@JSTests(value = { "http://localhost:10000/generic/api/ProxyFactoryTest.js" })
public class ProxyFactoryTest extends JSTestSuite {

    private final HttpServer server = new HttpServer(TestContext.JETTY_PORT);

    @Override
    protected void init() {
        super.setUp();
        try {
            JNDIHelper.getDefaultContext().bind("/add", new Addition());
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void cleanUp() {
        super.tearDown();
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
