package org.zenframework.easyservices.test;

import javax.naming.Context;

import org.junit.After;
import org.junit.Before;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.http.ServiceHttpRequestHandler;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.jndi.JNDIHelper;

import junit.framework.TestCase;

public abstract class AbstractServiceTest extends TestCase {

    private final Context serviceRegistry = JNDIHelper.getDefaultContext();
    private ClientFactory clientFactory;
    private HttpServer server;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clientFactory = new ClientFactoryImpl("http://localhost:" + TestContext.JETTY_PORT + "/services");
        server = new HttpServer(TestContext.JETTY_PORT);
        server.setServiceHttpRequestHandler(new ServiceHttpRequestHandler());
        server.start();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    protected <T> T getClient(Class<T> serviceClass, String serviceName) throws ClientException {
        return clientFactory.getClient(serviceClass, serviceName);
    }

    protected Context getServiceRegistry() {
        return serviceRegistry;
    }

}
