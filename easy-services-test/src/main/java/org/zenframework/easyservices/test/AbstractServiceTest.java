package org.zenframework.easyservices.test;

import javax.naming.Context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.jndi.JNDIHelper;

import junit.framework.TestCase;

public abstract class AbstractServiceTest extends TestCase {

    protected static final ApplicationContext CONTEXT = new ClassPathXmlApplicationContext("classpath:default-context.xml");

    private HttpServer server = CONTEXT.getBean(HttpServer.class);

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
        return CONTEXT.getBean(ClientFactory.class).getClient(serviceClass, serviceName);
    }

    protected static Context getServiceRegistry() {
        return JNDIHelper.getDefaultContext();
    }

}
