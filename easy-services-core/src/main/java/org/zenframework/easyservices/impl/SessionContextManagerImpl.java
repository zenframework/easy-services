package org.zenframework.easyservices.impl;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.SessionContextManager;
import org.zenframework.easyservices.jndi.SecureContext;
import org.zenframework.easyservices.util.JNDIUtil;

public class SessionContextManagerImpl implements SessionContextManager {

    public static final String SESSION_DOMAIN = "session";
    //public static final String DYNAMIC_DOMAIN = "dynamic";

    private Context serviceRegistry = JNDIUtil.getDefaultContext();
    private boolean securityEnabled = Environment.isSecurityEnabled();

    @Override
    public Context getSecureServiceRegistry(String sessionId) {
        Context sessionServiceRegistry = serviceRegistry;
        try {
            //Name sessionDomain = JNDIUtil.compose(serviceRegistry, SESSION_DOMAIN, sessionId);
            //Name sharedDomain = JNDIUtil.compose(serviceRegistry, DYNAMIC_DOMAIN);
            // shared objects context
            //sessionServiceRegistry = new SharedContext(serviceRegistry, sessionDomain, sharedDomain);
            // secure context
            if (securityEnabled)
                sessionServiceRegistry = new SecureContext(sessionServiceRegistry, false, getRule(SecureContext.ALL, SESSION_DOMAIN, sessionId),
                        getRule(SecureContext.NONE, SESSION_DOMAIN), /*getRule(SecureContext.NONE, DYNAMIC_DOMAIN),*/ getRule(SecureContext.ALL));
        } catch (NamingException e) {
            throw new ServiceException("Can't create service session " + sessionId, e);
        }
        return sessionServiceRegistry;
    }

    @Override
    public Name getSessionContextName(String sessionId) {
        try {
            return JNDIUtil.compose(serviceRegistry, SESSION_DOMAIN, sessionId);
        } catch (NamingException e) {
            throw new ServiceException("Can't compose session " + sessionId + " context name ", e);
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

    protected SecureContext.Rule getRule(int access, String... nameComponents) throws NamingException {
        return new SecureContext.Rule(JNDIUtil.compose(serviceRegistry, nameComponents), access);
    }

}
