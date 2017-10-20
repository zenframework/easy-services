package org.zenframework.easyservices.test.js;

import org.zenframework.easyservices.js.junit.JSTestSuite;
import org.zenframework.easyservices.js.junit.JSTests;
import org.zenframework.easyservices.test.HttpServer;
import org.zenframework.easyservices.test.TestContext;
import org.zenframework.easyservices.test.descriptor.CollectionUtilImpl;
import org.zenframework.easyservices.test.simple.Addition;
import org.zenframework.easyservices.test.simple.EchoImpl;
import org.zenframework.easyservices.util.JNDIUtil;

@JSTests(value = { "classpath://export/generic/api/ProxyFactoryTest.js" })
public class ProxyFactoryTest extends JSTestSuite {

    private final HttpServer server = new HttpServer(TestContext.SERVER_PORT);

    @Override
    protected void setUp() {
        super.setUp();
        try {
            JNDIUtil.getDefaultContext().bind("add", new Addition());
            JNDIUtil.getDefaultContext().bind("util", new CollectionUtilImpl());
            JNDIUtil.getDefaultContext().bind("echo", new EchoImpl());
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void tearDown() {
        super.tearDown();
        try {
            JNDIUtil.getDefaultContext().unbind("add");
            JNDIUtil.getDefaultContext().unbind("util");
            JNDIUtil.getDefaultContext().unbind("echo");
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
