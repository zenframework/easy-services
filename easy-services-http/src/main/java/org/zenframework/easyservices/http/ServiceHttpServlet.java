package org.zenframework.easyservices.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.commons.web.GenericConfig;
import org.zenframework.easyservices.ServiceInvoker;

public class ServiceHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 6337141493514767757L;

    private final ServiceHttpRequestHandler requestHandler = new ServiceHttpRequestHandler();

    public void setErrorMapper(ErrorMapper errorMapper) {
        requestHandler.setErrorMapper(errorMapper);
    }

    public void setServicesPath(String servicesPath) {
        requestHandler.setServicesPath(servicesPath);
    }

    public void setServiceInvoker(ServiceInvoker serviceInvoker) {
        requestHandler.setServiceInvoker(serviceInvoker);
    }

    public ErrorMapper getErrorMapper() {
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
        GenericConfig config = new GenericConfig(getServletConfig());
        requestHandler.init(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requestHandler.handleRequest(req, resp);
    }

}
