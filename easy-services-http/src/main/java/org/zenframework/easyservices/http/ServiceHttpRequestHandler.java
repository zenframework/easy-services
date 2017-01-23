package org.zenframework.easyservices.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ErrorDescription;
import org.zenframework.easyservices.InvocationException;
import org.zenframework.easyservices.RequestContext;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceHttpRequestHandler {

    private static final String DEFAULT_SERVICE_INFO_ALIAS = "serviceInfo";

    private static final Logger LOG = LoggerFactory.getLogger(ServiceHttpRequestHandler.class);

    private InitialContext serviceRegistry;
    private SerializerFactory<?> serializerFactory;
    private RequestMapper requestMapper;
    private ErrorMapper errorMapper;
    private ServiceInvoker serviceInvoker;
    private String servicesPath = "/services";
    private String serviceInfoAlias = DEFAULT_SERVICE_INFO_ALIAS;

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Serializer<?> serializer = serializerFactory.getSerializer();
        int status = HttpServletResponse.SC_OK;
        Object result;
        try {
            RequestContext context = requestMapper.getRequestContext(getRequestURI(request), getContextPath(request));
            Object service = serviceRegistry.lookup(context.getServiceName());
            result = context.getMethodName().equals(serviceInfoAlias) ? serviceInvoker.getServiceInfo(service)
                    : serviceInvoker.invoke(context, service, serializer);
        } catch (Throwable e) {
            if (e instanceof ServiceException)
                LOG.warn(e.getMessage(), e);
            else
                LOG.error(e.getMessage(), e);
            if (e instanceof InvocationException)
                e = e.getCause();
            status = errorMapper == null ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : errorMapper.getStatus(e);
            result = new ErrorDescription(e);
        }
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(serializer.serialize(result));
    }

    public void setServiceRegistry(InitialContext serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSerializerFactory(SerializerFactory<?> serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setRequestMapper(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    public void setErrorMapper(ErrorMapper errorMapper) {
        this.errorMapper = errorMapper;
    }

    public void setServiceInvoker(ServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
    }

    public void setServiceInfoAlias(String serviceInfoAlias) {
        this.serviceInfoAlias = serviceInfoAlias;
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
