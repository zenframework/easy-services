package org.zenframework.easyservices.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.WebappConfig;

public class HttpServiceServlet extends HttpServlet {

    private static final long serialVersionUID = 6337141493514767757L;

    private final HttpServiceRequestHandler requestHandler = new HttpServiceRequestHandler();

    private Config config;

    public void setErrorMapper(HttpErrorMapper errorMapper) {
        requestHandler.setErrorMapper(errorMapper);
    }

    public void setServicesPath(String servicesPath) {
        requestHandler.setServicesPath(servicesPath);
    }

    public void setServiceInvoker(ServiceInvoker serviceInvoker) {
        requestHandler.setServiceInvoker(serviceInvoker);
    }

    public HttpErrorMapper getErrorMapper() {
        return requestHandler.getErrorMapper();
    }

    public String getServicesPath() {
        return requestHandler.getServicesPath();
    }

    public ServiceInvoker getServiceInvoker() {
        return requestHandler.getServiceInvoker();
    }

    @Override
    public void init() throws ServletException {
        config = new WebappConfig(getServletConfig());
        requestHandler.init(config);
    }

    @Override
    public void destroy() {
        requestHandler.destroy(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requestHandler.handleRequest(req, resp);
    }

}
