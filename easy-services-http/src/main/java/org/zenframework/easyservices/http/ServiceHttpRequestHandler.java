package org.zenframework.easyservices.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.bean.PrettyStringBuilder;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ErrorDescription;
import org.zenframework.easyservices.InvocationException;
import org.zenframework.easyservices.RequestContext;
import org.zenframework.easyservices.ServiceInfo;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.RequestMapper;

public class ServiceHttpRequestHandler {

    private static final String DEFAULT_SERVICE_INFO_ALIAS = "serviceInfo";

    private static final Logger LOG = LoggerFactory.getLogger(ServiceHttpRequestHandler.class);

    private InitialContext serviceRegistry;
    private SerializerFactory<?> serializerFactory;
    private RequestMapper requestMapper;
    private ErrorMapper errorMapper;
    private ServiceInfo serviceInfo;
    private String servicesPath = "/services";
    private String serviceInfoAlias = DEFAULT_SERVICE_INFO_ALIAS;

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        RequestContext context;
        Serializer<?> serializer = serializerFactory.getSerializer();
        try {
            String contextPath = request.getContextPath();
            context = requestMapper.getRequestContext(getRequestURI(request), (contextPath != null ? contextPath : "") + servicesPath);
            if (context.getMethodName().equals(serviceInfoAlias))
                callServiceInfo(context, response, serializer);
            else
                callService(context, response, serializer);
        } catch (Throwable e) {
            sendError(response, e, serializer);
        }
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

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
    }

    public void setServiceInfoAlias(String serviceInfoAlias) {
        this.serviceInfoAlias = serviceInfoAlias;
    }

    private void callService(RequestContext context, HttpServletResponse response, Serializer<?> serializer)
            throws IOException, ServiceException, InvocationException, NamingException {

        Method method = null;
        Object args[] = null;

        Object service = serviceRegistry.lookup(context.getServiceName());
        for (Method m : service.getClass().getMethods()) {
            if (m.getName().equals(context.getMethodName())) {
                try {
                    args = serializer.deserialize(context.getArguments(), m.getParameterTypes());
                    method = m;
                    break;
                } catch (SerializationException e) {
                    LOG.debug("Can't convert given args " + context.getArguments() + " to method '" + context.getMethodName() + "' argument types "
                            + m.getParameterTypes());
                }
            }
        }

        if (method == null)
            throw new ServiceException("Can't find method [" + context.getMethodName() + "] applicable for given args " + context.getArguments());

        Object result;
        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker(new StringBuilder(1024).append(context.getServiceName()).append('.').append(context.getMethodName())
                    .append(new PrettyStringBuilder().toString(args)).toString(), LOG);
        try {
            result = method.invoke(service, args);
            if (time != null)
                time.printDifference(result);
            response.getWriter().print(serializer.serialize(result));
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException)
                e = ((InvocationTargetException) e).getTargetException();
            if (time != null)
                time.printDifference(e);
            throw new InvocationException(context, args, e);
        }

    }

    private void callServiceInfo(RequestContext context, HttpServletResponse response, Serializer<?> serializer) throws IOException, NamingException {
        response.getWriter().write(serializer.serialize(serviceInfo.getServiceInfo(serviceRegistry.lookup(context.getServiceName()))));
    }

    private void sendError(HttpServletResponse response, Throwable e, Serializer<?> serializer) throws IOException {
        if (e instanceof ServiceException) {
            LOG.warn(e.getMessage(), e);
        } else {
            LOG.error(e.getMessage(), e);
        }
        if (e instanceof InvocationException) {
            e = e.getCause();
        }
        int status = errorMapper == null ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : errorMapper.getStatus(e);
        response.setStatus(status);
        response.getWriter().print(serializer.serialize(new ErrorDescription(e)));
    }

    private static URI getRequestURI(HttpServletRequest request) throws URISyntaxException, UnsupportedEncodingException {
        return new URI(request.getRequestURL().append('?').append(URLEncoder.encode(request.getQueryString(), "UTF-8")).toString());
    }

}
