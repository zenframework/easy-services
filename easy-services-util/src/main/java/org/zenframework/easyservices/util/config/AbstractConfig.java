package org.zenframework.easyservices.util.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfig implements Config {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfig.class);

    private final Map<String, Object> instancesCache = new HashMap<String, Object>();

    @Override
    public Config getSubConfig(String prefix) {
        return new SubConfig(this, prefix);
    }

    @Override
    public Object getRequiredParam(String name) {
        Object value = getParam(name);
        if (value == null)
            throw new ConfigException("Parameter " + name + " required");
        return value;
    }

    @Override
    public String getParam(String name, String defaultValue) {
        Object value = getParam(name);
        return value != null ? value.toString() : defaultValue;
    }

    @Override
    public int getParam(String name, int defaultValue) {
        Object value = getParam(name);
        return value != null ? Integer.parseInt(value.toString()) : defaultValue;
    }

    @Override
    public boolean getParam(String name, boolean defaultValue) {
        Object value = getParam(name);
        return value != null ? Boolean.parseBoolean(value.toString()) : defaultValue;
    }

    @Override
    public String[] getParam(String name, String[] defaultValue) {
        Object value = getParam(name);
        return value != null ? value.toString().trim().split("\\s*\\,\\s*") : defaultValue;
    }

    @Override
    public Class<?> getParam(String name, Class<?> defaultValue) {
        Object value = getParam(name);
        try {
            return value != null ? Class.forName(value.toString()) : defaultValue;
        } catch (ClassNotFoundException e) {
            throw new ConfigException("Can't get class param " + name, e);
        }
    }

    @Override
    public File getAbsolutePath(String relativePath) {
        return new File(getParam(PROP_WORKING_DIRECTORY, "."), relativePath);
    }

    @Override
    public Object getInstance(String name) {
        return getInstance(name, getParam(name), null, false, true /*false*/);
    }

    @Override
    public Object getInstance(String name, Object defaultValue) {
        return getInstance(name, getParam(name), defaultValue, false, true /*false*/);
    }

    @Override
    public Object getInstance(String name, Class<?> defaultValueClass) {
        return getInstance(name, getParam(name), defaultValueClass, false, true /*false*/);
    }

    // @Override
    // public Object getConfigurableInstance(String name) {
    //     return getInstance(name, getParam(name), null, false, true);
    // }

    // @Override
    // public Object getConfigurableInstance(String name, Class<? extends Configurable> defaultValueClass) {
    //     return getInstance(name, getParam(name), defaultValueClass, false, true);
    // }

    @Override
    public void destroyInstances(Object... instances) {
        for (Object instance : instances) {
            if (instance != null) {
                Iterator<Map.Entry<String, Object>> entries = instancesCache.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<String, Object> entry = entries.next();
                    if (entry.getValue() == instance) {
                        entries.remove();
                        if (instance instanceof Configurable) {
                            try {
                                ((Configurable) instance).destroy(this);
                            } catch (Throwable e) {
                                LOG.warn("Can't destroy configurable " + entry.getKey() + " = " + entry.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString(boolean prettyPrint) {
        List<String> names = getNames();
        Collections.sort(names);
        StringBuilder str = new StringBuilder("CONFIGURATION {");
        for (String name : names)
            str.append(prettyPrint ? "\n\t" : str.charAt(str.length() - 1) == '{' ? " " : ", ").append(name).append(" = ").append(getParam(name));
        str.append(prettyPrint && !names.isEmpty() ? '\n' : ' ').append('}');
        return str.toString();
    }

    protected Config getRootConfig() {
        return this;
    }

    protected Object getCachedInstance(String name) {
        return instancesCache.get(name);
    }

    protected void cacheInstance(String name, Object instance) {
        instancesCache.put(name, instance);
    }

    private Object getInstance(String name, Object refOrClassName, Object defaultInstanceOrClass, boolean rootConfig, boolean configurable) {

        // search cache
        Object instanceOrClass = getCachedInstance(name);
        if (instanceOrClass != null)
            return instanceOrClass;

        if (refOrClassName == null) {
            // param is not set, use default class
            instanceOrClass = defaultInstanceOrClass;
        } else {
            // is ref?
            String nextName = refOrClassName.toString();
            Object nextRefOrClassName = getRootConfig().getParam(nextName);
            if (nextRefOrClassName != null)
                // refOrClassName is ref
                return getInstance(nextName, nextRefOrClassName, defaultInstanceOrClass, true, configurable);
            // refOrClassName is class name
            try {
                instanceOrClass = Class.forName(nextName);
            } catch (ClassNotFoundException e) {
                throw new ConfigException("Parameter " + name + " = " + nextName + " is not a class name, or class not found", e);
            }
        }

        if (instanceOrClass == null)
            return null;

        try {
            if (instanceOrClass instanceof Class)
                instanceOrClass = ((Class<?>) instanceOrClass).newInstance();
        } catch (Exception e) {
            throw new ConfigException("Can't instantiate " + instanceOrClass, e);
        }

        if (configurable && instanceOrClass instanceof Configurable) {
            Config config = (rootConfig ? getRootConfig() : this).getSubConfig(name);
            LOG.debug("Initialize configurable " + name + " with " + config.toString(true));
            ((Configurable) instanceOrClass).init(config);
        }

        cacheInstance(name, instanceOrClass);
        return instanceOrClass;

    }

}
