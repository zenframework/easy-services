package org.zenframework.easyservices.http;

import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.easyservices.ServiceInfo;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.RequestMapper;

public class ServiceHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 6337141493514767757L;

    private static final String PARAM_INITIAL_CONTEXT_FACTORY = "initialContextFactory";
    private static final String PARAM_SERIALIZER_FACTORY = "serializerFactory";
    private static final String PARAM_REQUEST_MAPPER = "requestMapper";
    private static final String PARAM_ERROR_MAPPER = "errorMapper";
    private static final String PARAM_SERVICE_INFO = "serviceInfo";
    private static final String PARAM_SERVICE_INFO_ALIAS = "serviceInfoAlias";

    private static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "org.zenframework.easyservices.jndo.InitialContextFactoryImpl";

    private final ServiceHttpRequestHandler requestHandler = new ServiceHttpRequestHandler();

    @Override
    public void init() throws ServletException {
        requestHandler.setServiceRegistry(getRegistry());
        requestHandler.setSerializerFactory(getInstance(SerializerFactory.class, PARAM_SERIALIZER_FACTORY));
        requestHandler.setRequestMapper(getInstance(RequestMapper.class, PARAM_REQUEST_MAPPER));
        requestHandler.setErrorMapper(getInstance(ErrorMapper.class, PARAM_ERROR_MAPPER));
        requestHandler.setServiceInfo(getInstance(ServiceInfo.class, PARAM_SERVICE_INFO));
        String serviceInfoAlias = getServletConfig().getInitParameter(PARAM_SERVICE_INFO_ALIAS);
        if (serviceInfoAlias != null)
            requestHandler.setServiceInfoAlias(serviceInfoAlias);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requestHandler.handleRequest(req, resp);
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(Class<T> cls, String param) throws ServletException {
        String className = getServletConfig().getInitParameter(param);
        if (className == null)
            throw new ServletException("Servlet config parameter '" + param + "' undefined");
        try {
            return (T) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new ServletException("Can't get instance of '" + className + "'", e);
        }
    }

    private InitialContext getRegistry() throws ServletException {
        String initialContextFactory = getServletConfig().getInitParameter(PARAM_INITIAL_CONTEXT_FACTORY);
        if (initialContextFactory == null)
            initialContextFactory = DEFAULT_INITIAL_CONTEXT_FACTORY;
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        try {
            return new InitialContext(props);
        } catch (NamingException e) {
            throw new ServletException("Can't initialize JNDI context", e);
        }
    }

}
