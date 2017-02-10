package org.zenframework.easyservices.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.config.Config;
import org.zenframework.commons.config.Configurable;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;

public class ServiceHttpRequestHandler implements Configurable {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ServiceHttpRequestHandler.class);

    private static final String PARAM_ERROR_MAPPER = "errorMapper";
    private static final String PARAM_SERVICES_PATH = "servicesPath";
    private static final String PARAM_SERVICE_INVOKER = "serviceInvoker";

    private static final String DEFAULT_SERVICES_PATH = "/services";

    private ErrorMapper errorMapper = new ErrorMapper();
    private String servicesPath = DEFAULT_SERVICES_PATH;
    private ServiceInvoker serviceInvoker = new ServiceInvokerImpl();

    @Override
    public void init(Config config) {
        errorMapper = (ErrorMapper) config.getInstance(PARAM_ERROR_MAPPER, errorMapper);
        servicesPath = config.getParam(PARAM_SERVICES_PATH, servicesPath);
        serviceInvoker = (ServiceInvoker) config.getInstance(PARAM_SERVICE_INVOKER, serviceInvoker);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(errorMapper, serviceInvoker);
    }

    public void handleRequest(HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        serviceInvoker.invoke(new HttpServiceRequest(request, servicesPath), new HttpServiceResponse(response, errorMapper));
    }

    public void setErrorMapper(ErrorMapper errorMapper) {
        this.errorMapper = errorMapper;
    }

    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
    }

    public void setServiceInvoker(ServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public ErrorMapper getErrorMapper() {
        return errorMapper;
    }

    public String getServicesPath() {
        return servicesPath;
    }

    public ServiceInvoker getServiceInvoker() {
        return serviceInvoker;
    }

}
