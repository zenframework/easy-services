package org.zenframework.easyservices.test;

import javax.naming.Context;

import org.junit.After;
import org.junit.Before;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.http.HttpServiceRequestHandler;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.net.TcpServer;
import org.zenframework.easyservices.tcp.TcpServiceRequestHandler;
import org.zenframework.easyservices.util.jndi.JNDIHelper;

import junit.framework.TestCase;

public abstract class AbstractServiceTest extends TestCase {

    private final Context serviceRegistry = JNDIHelper.getDefaultContext();

    private final String protocol;

    private ClientFactory clientFactory;
    private HttpServer httpServer;
    private TcpServer tcpServer;

    protected AbstractServiceTest(String protocolFormat) {
        String[] pair = protocolFormat.split("/");
        this.protocol = pair[0].trim();
        Environment.setSerializationFormat(pair[1].trim());
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        if ("http".equals(protocol)) {
            clientFactory = new ClientFactoryImpl("http://localhost:" + TestContext.SERVER_PORT + "/services");
            httpServer = new HttpServer(TestContext.SERVER_PORT);
            httpServer.setServiceHttpRequestHandler(new HttpServiceRequestHandler());
            httpServer.start();
        } else if ("tcp".equals(protocol)) {
            clientFactory = new ClientFactoryImpl("tcp://localhost:" + TestContext.SERVER_PORT);
            tcpServer = new TcpServer(TestContext.SERVER_PORT, new TcpServiceRequestHandler());
            tcpServer.start();
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (httpServer != null)
            httpServer.stop();
        if (tcpServer != null)
            tcpServer.stop();
        super.tearDown();
    }

    protected <T> T getClient(Class<T> serviceClass, String serviceName) throws ClientException {
        return clientFactory.getClient(serviceClass, serviceName);
    }

    protected Context getServiceRegistry() {
        return serviceRegistry;
    }

    protected static Object[] arr(Object... objs) {
        return objs;
    }

}
