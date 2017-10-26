package org.zenframework.easyservices.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.SessionContextManager;
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;
import org.zenframework.easyservices.impl.SessionContextManagerImpl;

public class HttpServiceRequestHandler implements Configurable {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(HttpServiceRequestHandler.class);

    private static final String PARAM_ERROR_MAPPER = "errorMapper";
    private static final String PARAM_SERVICES_PATH = "servicesPath";
    private static final String PARAM_SERVICE_INVOKER = "serviceInvoker";

    private static final String DEFAULT_SERVICES_PATH = "/services";

    private static final String ATTR_SERVICE_SESSION = "easyservices.session";

    private HttpErrorMapper errorMapper = new HttpErrorMapper();
    private String servicesPath = DEFAULT_SERVICES_PATH;
    private SessionContextManager sessionContextManager = new SessionContextManagerImpl();
    private ServiceInvoker serviceInvoker = new ServiceInvokerImpl();

    @Override
    public void init(Config config) {
        errorMapper = config.getInstance(PARAM_ERROR_MAPPER, errorMapper);
        servicesPath = config.getParam(PARAM_SERVICES_PATH, servicesPath);
        serviceInvoker = config.getInstance(PARAM_SERVICE_INVOKER, serviceInvoker);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(errorMapper, serviceInvoker);
    }

    public void handleRequest(HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        HttpSession httpSession = request.getSession();
        String sessionId = httpSession.getId();
        ServiceSession serviceSession = (ServiceSession) httpSession.getAttribute(ATTR_SERVICE_SESSION);
        if (serviceSession == null) {
            serviceSession = new HttpServiceSession(sessionId, sessionContextManager.getSecureServiceRegistry(sessionId),
                    sessionContextManager.getSessionContextName(sessionId));
            httpSession.setAttribute(ATTR_SERVICE_SESSION, serviceSession);
        }
        serviceInvoker.invoke(new HttpServiceRequest(serviceSession, request, servicesPath), new HttpServiceResponse(response, errorMapper));
    }

    public HttpErrorMapper getErrorMapper() {
        return errorMapper;
    }

    public void setErrorMapper(HttpErrorMapper errorMapper) {
        this.errorMapper = errorMapper;
    }

    public String getServicesPath() {
        return servicesPath;
    }

    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
    }

    public ServiceInvoker getServiceInvoker() {
        return serviceInvoker;
    }

    public void setServiceInvoker(ServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public SessionContextManager getSessionContextManager() {
        return sessionContextManager;
    }

    public void setSessionContextManager(SessionContextManager sessionContextManager) {
        this.sessionContextManager = sessionContextManager;
    }

}
