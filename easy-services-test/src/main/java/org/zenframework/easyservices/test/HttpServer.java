package org.zenframework.easyservices.test;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.zenframework.easyservices.http.ServiceHttpRequestHandler;
import org.zenframework.easyservices.resource.ClasspathResourceFactory;
import org.zenframework.easyservices.resource.Resource;
import org.zenframework.easyservices.resource.ResourceFactory;

public class HttpServer {

    private final int port;
    private final ResourceFactory resourceFactory = new ClasspathResourceFactory();
    private ServiceHttpRequestHandler serviceHttpRequestHandler = new ServiceHttpRequestHandler();

    private Server server;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {

        server = new Server(port);

        // Specify the Session ID Manager
        SessionIdManager idmanager = new HashSessionIdManager();
        server.setSessionIdManager(idmanager);

        // Sessions are bound to a context.
        ContextHandler context = new ContextHandler("/");
        server.setHandler(context);

        // Create the SessionHandler (wrapper) to handle the sessions
        SessionHandler sessions = new SessionHandler(new HashSessionManager());
        context.setHandler(sessions);

        // Put handler inside of SessionHandler 
        sessions.setHandler(new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                Resource resource = resourceFactory.getResource(baseRequest.getRequestURI());
                if (resource.exists()) {
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    InputStream in = resource.openStream();
                    try {
                        response.getWriter().write(IOUtils.toString(in, "UTF-8"));
                    } finally {
                        in.close();
                    }
                } else if (serviceHttpRequestHandler != null) {
                    serviceHttpRequestHandler.handleRequest(request, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                baseRequest.setHandled(true);
            }

        });

        server.start();

    }

    public void stop() throws Exception {
        server.stop();
    }

    public void setServiceHttpRequestHandler(ServiceHttpRequestHandler httpServiceRequestHandler) {
        this.serviceHttpRequestHandler = httpServiceRequestHandler;
    }

}
