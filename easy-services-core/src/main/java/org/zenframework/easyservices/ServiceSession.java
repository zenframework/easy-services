package org.zenframework.easyservices;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceSession {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceSession.class);

    private final String id;
    private final Context serviceRegistry;
    private final Name sessionContextName;

    public ServiceSession(String id, Context serviceRegistry, Name sessionContextName) {
        this.id = id;
        this.serviceRegistry = serviceRegistry;
        this.sessionContextName = sessionContextName;
    }

    public String getId() {
        return id;
    }

    public Context getServiceRegistry() {
        return serviceRegistry;
    }

    public Name getSessionContextName() {
        return sessionContextName;
    }

    public void invalidate() {
        try {
            getServiceRegistry().destroySubcontext(getSessionContextName());
        } catch (NamingException e) {
            LOG.error("Can't free session context " + getSessionContextName(), e);
        }
    }

}
