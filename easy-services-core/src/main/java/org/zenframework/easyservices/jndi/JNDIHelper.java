package org.zenframework.easyservices.jndi;

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.zenframework.easyservices.jndi.impl.InitialContextFactoryImpl;

public class JNDIHelper {

    public static InitialContext getInitialContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, InitialContextFactoryImpl.class.getName());
        return new InitialContext(props);
    }

    public static InitialContext getInitialContext(Map<?, ?> environment) throws NamingException {
        return getInitialContext(environment, null);
    }

    public static InitialContext getInitialContext(Map<?, ?> environment, Map<String, Object> bindings)
            throws NamingException {
        Properties props = new Properties();
        props.putAll(environment);
        return bindAll(new InitialContext(props), bindings);
    }

    public static InitialContext bindAll(InitialContext context, Map<String, Object> bindings) throws NamingException {
        if (bindings != null) {
            for (Map.Entry<String, Object> entry : bindings.entrySet())
                context.bind(entry.getKey(), entry.getValue());
        }
        return context;
    }

}
