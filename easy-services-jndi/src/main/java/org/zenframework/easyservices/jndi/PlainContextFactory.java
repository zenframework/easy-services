package org.zenframework.easyservices.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

@Deprecated
public class PlainContextFactory implements InitialContextFactory {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Context context = new PlainContext();
        context.getEnvironment().putAll((Hashtable) environment);
        return context;
    }

}
