package org.zenframework.easyservices.test;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.http.ServiceHttpRequestHandler;

import junit.framework.TestCase;

public class AbstractServiceTest extends TestCase {

    protected static final ApplicationContext CONTEXT = new ClassPathXmlApplicationContext("classpath:default-context.xml");

    private final int jettyPort = CONTEXT.getBean("jettyPort", Integer.class);
    private final ServiceHttpRequestHandler httpRequestHandler = CONTEXT.getBean(ServiceHttpRequestHandler.class);

    private Server server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = new Server(jettyPort);
        server.setHandler(new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                httpRequestHandler.handleRequest(request, response);
                baseRequest.setHandled(true);
            }

        });
        server.start();
    }

    @Override
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    protected static <T> T getClient(Class<T> serviceClass, String serviceName) {
        return CONTEXT.getBean(ClientFactory.class).getClient(serviceClass, serviceName);
    }

    protected static InitialContext getServiceRegistry() {
        return CONTEXT.getBean(InitialContext.class);
    }

}
