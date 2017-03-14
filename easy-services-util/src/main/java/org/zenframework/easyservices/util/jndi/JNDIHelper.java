package org.zenframework.easyservices.util.jndi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JNDIHelper {

    private static final Logger LOG = LoggerFactory.getLogger(JNDIHelper.class);

    private static final Set<InitialContextFactory> FACTORIES = new HashSet<InitialContextFactory>();
    private static final Context DEFAULT_CONTEXT;

    static {
        ServiceLoader<InitialContextFactory> loader = ServiceLoader.load(InitialContextFactory.class);
        Iterator<InitialContextFactory> it = loader.iterator();
        StringBuilder str = new StringBuilder().append("Initial context factories:");
        while (it.hasNext()) {
            InitialContextFactory factory = it.next();
            FACTORIES.add(factory);
            str.append("\n\t- " + factory);
        }
        if (FACTORIES.isEmpty())
            str.append("\n\t - <not found>");
        if (FACTORIES.isEmpty()) {
            LOG.warn(str.toString());
            DEFAULT_CONTEXT = null;
        } else {
            LOG.info(str.toString());
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, FACTORIES.iterator().next().getClass().getName());
            DEFAULT_CONTEXT = getInitialContext(props);
        }
    }

    private JNDIHelper() {}

    public static Context getDefaultContext() {
        return DEFAULT_CONTEXT;
    }

    public static Set<InitialContextFactory> getInitialContextFactories() {
        return Collections.unmodifiableSet(FACTORIES);
    }

    @SuppressWarnings("rawtypes")
    public static Context getInitialContext(Hashtable<?, ?> env) {
        try {
            return NamingManager.getInitialContext((Hashtable) env);
        } catch (NamingException e) {
            LOG.error("Can't initialize initial context " + env, e);
            return null;
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

}
