package org.zenframework.easyservices.jndi;

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.jndi.impl.InitialContextFactoryImpl;
import org.zenframework.easyservices.util.config.Config;
import org.zenframework.easyservices.util.config.ConfigException;

public class JNDIHelper {

    private static final Logger LOG = LoggerFactory.getLogger(JNDIHelper.class);

    private static Context DEFAULT_CONTEXT = initDefaultContext();

    private JNDIHelper() {}

    public static Context getDefaultContext() {
        return DEFAULT_CONTEXT;
    }

    public static Context newDefaultContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, InitialContextFactoryImpl.class.getName());
        return NamingManager.getInitialContext(props);
    }

    public static Context newDefaultContext(Config config) throws ConfigException {
        Properties props = new Properties();
        for (String name : config.getNames())
            props.put(name, config.getParam(name));
        try {
            return NamingManager.getInitialContext(props);
        } catch (NamingException e) {
            throw new ConfigException("Can't initialize default JNDI context", e);
        }
    }

    public static Context bindAll(Map<String, Object> bindings) throws NamingException {
        return bindAll(DEFAULT_CONTEXT, bindings);
    }

    public static Context bindAll(Context context, Map<String, Object> bindings) throws NamingException {
        if (bindings != null) {
            for (Map.Entry<String, Object> entry : bindings.entrySet())
                context.bind(entry.getKey(), entry.getValue());
        }
        return context;
    }

    private static Context initDefaultContext() {
        try {
            return newDefaultContext();
        } catch (NamingException e) {
            LOG.error("Can't initialize default context", e);
            return null;
        }
    }

}
