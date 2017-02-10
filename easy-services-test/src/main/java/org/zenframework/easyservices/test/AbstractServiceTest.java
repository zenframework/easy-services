package org.zenframework.easyservices.test;

import javax.naming.Context;

import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.jndi.JNDIHelper;

import junit.framework.TestCase;

public abstract class AbstractServiceTest extends TestCase {

    private HttpServer server = TestContext.CONTEXT.getBean(HttpServer.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server.start();
    }

    @Override
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    protected static <T> T getClient(Class<T> serviceClass, String serviceName) throws ClientException {
        return TestContext.CONTEXT.getBean(ClientFactory.class).getClient(serviceClass, serviceName);
    }

    protected static Context getServiceRegistry() {
        return JNDIHelper.getDefaultContext();
    }

}
