package org.zenframework.easyservices.test;

import javax.naming.Context;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.http.HttpServiceRequestHandler;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.impl.ServiceMethodInterceptor;
import org.zenframework.easyservices.net.NioTcpServer;
import org.zenframework.easyservices.net.TcpServer;
import org.zenframework.easyservices.tcp.TcpServiceRequestHandler;
import org.zenframework.easyservices.tcp.TcpxClientFactory;
import org.zenframework.easyservices.tcp.TcpxServiceRequestHandler;
import org.zenframework.easyservices.util.JNDIUtil;
import org.zenframework.easyservices.util.debug.TimeStat;

import junit.framework.TestCase;

public abstract class AbstractServiceTest extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceTest.class);

    private final Context serviceRegistry = JNDIUtil.getDefaultContext();

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
        Environment.setDebug(true);
        TimeStat.clearTimeStat(ServiceInvoker.class, "invoke");
        TimeStat.clearTimeStat(ServiceMethodInterceptor.class, "intercept");
        TimeStat.setThreadTimeStat(ServiceInvoker.class, "invoke");
        if ("http".equals(protocol)) {
            clientFactory = new ClientFactoryImpl("http://localhost:" + TestContext.SERVER_PORT + "/services");
            httpServer = new HttpServer(TestContext.SERVER_PORT);
            httpServer.setServiceHttpRequestHandler(new HttpServiceRequestHandler());
            httpServer.start();
        } else if ("tcp".equals(protocol)) {
            clientFactory = new ClientFactoryImpl("tcp://localhost:" + TestContext.SERVER_PORT);
            tcpServer = new NioTcpServer(TestContext.SERVER_PORT, new TcpServiceRequestHandler());
            tcpServer.start();
        } else if ("tcpx".equals(protocol)) {
            clientFactory = new TcpxClientFactory("tcpx://localhost:" + TestContext.SERVER_PORT);
            tcpServer = new NioTcpServer(TestContext.SERVER_PORT, new TcpxServiceRequestHandler());
            tcpServer.start();
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        LOG.info(TimeStat.getTimeStat(ServiceInvoker.class, "invoke").toString());
        LOG.info(TimeStat.getTimeStat(ServiceMethodInterceptor.class, "intercept").toString());
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
