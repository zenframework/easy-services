package org.zenframework.easyservices.jndi.impl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class InitialContextFactoryImpl implements InitialContextFactory {

    private static final Context CONTEXT = new ContextImpl();
    
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return CONTEXT;
    }

}
