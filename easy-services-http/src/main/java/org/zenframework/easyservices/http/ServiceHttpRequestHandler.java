package org.zenframework.easyservices.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.config.Config;
import org.zenframework.commons.config.Configurable;
import org.zenframework.easyservices.IncorrectRequestException;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.ServiceResponse;
import org.zenframework.easyservices.impl.RequestMapperImpl;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;

public class ServiceHttpRequestHandler implements Configurable {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ServiceHttpRequestHandler.class);

    private static final String PARAM_REQUEST_MAPPER = "requestMapper";
    private static final String PARAM_ERROR_MAPPER = "errorMapper";
    private static final String PARAM_SERVICES_PATH = "servicesPath";
    private static final String PARAM_SERVICE_INVOKER = "serviceInvoker";

    private static final String DEFAULT_SERVICES_PATH = "/services";

    private RequestMapper requestMapper = RequestMapperImpl.INSTANCE;
    private ErrorMapper errorMapper = new ErrorMapper();
    private String servicesPath = DEFAULT_SERVICES_PATH;
    private ServiceInvoker serviceInvoker = new ServiceInvokerImpl();

    @Override
    public void init(Config config) {
        requestMapper = (RequestMapper) config.getInstance(PARAM_REQUEST_MAPPER, requestMapper);
        errorMapper = (ErrorMapper) config.getInstance(PARAM_ERROR_MAPPER, errorMapper);
        servicesPath = config.getParam(PARAM_SERVICES_PATH, servicesPath);
        serviceInvoker = (ServiceInvoker) config.getInstance(PARAM_SERVICE_INVOKER, serviceInvoker);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(errorMapper, serviceInvoker);
    }

    public void handleRequest(HttpServletRequest request, final HttpServletResponse response) throws IOException {
        try {
            ServiceRequest serviceRequest = requestMapper.getRequestContext(getRequestURI(request), getContextPath(request));
            ServiceResponse serviceResponse = serviceInvoker.invoke(serviceRequest);
            response.setCharacterEncoding("UTF-8");
            if (serviceResponse.isSuccess()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(serviceResponse.getSerializedResult());
            } else {
                response.sendError(
                        errorMapper != null ? errorMapper.getStatus(serviceResponse.getError()) : HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        serviceResponse.getSerializedResult());
            }
        } catch (IncorrectRequestException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw e;
        }
    }

    public void setRequestMapper(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    public RequestMapper getRequestMapper() {
        return requestMapper;
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

    private static URI getRequestURI(HttpServletRequest request) throws URISyntaxException, UnsupportedEncodingException {
        StringBuffer str = request.getRequestURL();
        if (request.getQueryString() != null)
            str.append('?').append(URLEncoder.encode(request.getQueryString(), "UTF-8"));
        return new URI(str.toString());
    }

    private String getContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if (contextPath == null)
            contextPath = "";
        return contextPath + servicesPath;
    }

}
