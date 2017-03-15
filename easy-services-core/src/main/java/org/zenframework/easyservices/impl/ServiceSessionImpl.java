package org.zenframework.easyservices.impl;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.util.jndi.JNDIHelper;

public class ServiceSessionImpl implements ServiceSession {

    private final String id;
    private final Context serviceRegistry;

    public ServiceSessionImpl(String id, Context serviceRegistry) {
        this.id = id;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Context getServiceRegistry() {
        return serviceRegistry;
    }

    @Override
    public String bindService(String localName, Object service) {
        Name name;
        try {
            name = JNDIHelper.compose(serviceRegistry, ServiceSessionManagerImpl.DOMAIN_SESSION, id, localName);
        } catch (NamingException e) {
            throw new ServiceException("Can't get service name " + localName, e);
        }
        try {
            serviceRegistry.lookupLink(name);
        } catch (NamingException e) {
            try {
                serviceRegistry.bind(name, service);
            } catch (NamingException e1) {
                throw new ServiceException("Can't bind service " + name, e1);
            }
        }
        return name.toString();
    }

    @Override
    public void unbindService(String name) {
        try {
            serviceRegistry.unbind(name);
        } catch (NamingException e) {
            throw new ServiceException("Can't unbind service " + name + ": " + e.getMessage(), e);
        }
    }

}
