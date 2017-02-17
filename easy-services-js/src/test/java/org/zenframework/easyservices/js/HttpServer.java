package org.zenframework.easyservices.js;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.zenframework.easyservices.util.resource.ClasspathResourceFactory;
import org.zenframework.easyservices.util.resource.Resource;
import org.zenframework.easyservices.util.resource.ResourceFactory;

public class HttpServer {

    private final int port = 10000;
    private final ResourceFactory resourceFactory = new ClasspathResourceFactory();

    private Server server;

    public void start() throws Exception {
        server = new Server(port);
        server.setHandler(new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                Resource resource = resourceFactory.getResource(baseRequest.getRequestURI());
                response.setCharacterEncoding("UTF-8");
                if (resource.exists()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    InputStream in = resource.openStream();
                    try {
                        response.getWriter().write(IOUtils.toString(in, "UTF-8"));
                    } finally {
                        in.close();
                    }
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

}
