package org.zenframework.easyservices.http;

import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;
import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 6337141493514767757L;

    private static final String PARAM_INITIAL_CONTEXT_FACTORY = "initialContextFactory";
    private static final String PARAM_SERIALIZER_FACTORY = "serializerFactory";
    private static final String PARAM_REQUEST_MAPPER = "requestMapper";
    private static final String PARAM_ERROR_MAPPER = "errorMapper";
    private static final String PARAM_SERVICE_INVOKER = "serviceInvoker";
    private static final String PARAM_SERVICE_INFO_ALIAS = "serviceInfoAlias";

    private final ServiceHttpRequestHandler requestHandler = new ServiceHttpRequestHandler();

    @Override
    public void init() throws ServletException {
        ServiceInvokerImpl serviceInvoker = new ServiceInvokerImpl();
        serviceInvoker.setServiceRegistry(getRegistry());
        serviceInvoker.setSerializerFactory(getInstance(SerializerFactory.class, PARAM_SERIALIZER_FACTORY));
        serviceInvoker.setRequestMapper(getInstance(RequestMapper.class, PARAM_REQUEST_MAPPER));
        requestHandler.setServiceInvoker(serviceInvoker);
        requestHandler.setErrorMapper(getInstance(ErrorMapper.class, PARAM_ERROR_MAPPER));
        requestHandler.setServiceInvoker(getInstance(ServiceInvoker.class, PARAM_SERVICE_INVOKER));
        String serviceInfoAlias = getServletConfig().getInitParameter(PARAM_SERVICE_INFO_ALIAS);
        if (serviceInfoAlias != null)
            serviceInvoker.setServiceInfoAlias(serviceInfoAlias);
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

    private Context getRegistry() throws ServletException {
        String initialContextFactory = getServletConfig().getInitParameter(PARAM_INITIAL_CONTEXT_FACTORY);
        if (initialContextFactory != null) {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            try {
                return NamingManager.getInitialContext(props);
            } catch (NamingException e) {
                throw new ServletException("Can't initialize JNDI context", e);
            }
        }
        return JNDIHelper.getDefaultContext();
    }

}
