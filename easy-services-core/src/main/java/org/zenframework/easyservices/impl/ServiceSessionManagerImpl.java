package org.zenframework.easyservices.impl;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.ServiceSessionManager;
import org.zenframework.easyservices.jndi.SecureContext;
import org.zenframework.easyservices.jndi.SharedContext;
import org.zenframework.easyservices.util.jndi.JNDIHelper;

public class ServiceSessionManagerImpl implements ServiceSessionManager {

    public static final String DOMAIN_SESSION = "session";
    public static final String DOMAIN_DYNAMIC = "dynamic";

    private final Map<String, ServiceSession> sessions = new HashMap<String, ServiceSession>();

    private Context serviceRegistry = JNDIHelper.getDefaultContext();
    private boolean securityEnabled = Environment.isSecurityEnabled();

    @Override
    public ServiceSession getSession(String sessionId) {
        synchronized (sessions) {
            ServiceSession session = sessions.get(sessionId);
            if (session == null) {
                session = newSession(sessionId);
                sessions.put(session.getId(), session);
            }
            return session;
        }
    }

    public Context getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(Context serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    protected ServiceSession newSession(String sessionId) {
        Context sessionServiceRegistry = serviceRegistry;
        try {
            Name sessionDomain = JNDIHelper.compose(serviceRegistry, DOMAIN_SESSION, sessionId);
            Name sharedDomain = JNDIHelper.compose(serviceRegistry, DOMAIN_DYNAMIC);
            // shared objects context
            sessionServiceRegistry = new SharedContext(serviceRegistry, sessionDomain, sharedDomain);
            // secure context
            if (securityEnabled)
                sessionServiceRegistry = new SecureContext(sessionServiceRegistry, false, getRule(SecureContext.ALL, DOMAIN_SESSION, sessionId),
                        getRule(SecureContext.NONE, DOMAIN_SESSION), getRule(SecureContext.NONE, DOMAIN_DYNAMIC), getRule(SecureContext.ALL));
        } catch (NamingException e) {
            throw new ServiceException("Can't create service session " + sessionId, e);
        }
        return new ServiceSessionImpl(sessionId, sessionServiceRegistry);
    }

    protected SecureContext.Rule getRule(int access, String... nameComponents) throws NamingException {
        return new SecureContext.Rule(JNDIHelper.compose(serviceRegistry, nameComponents), access);
    }

}
