package org.zenframework.easyservices.config;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectiveConfig extends AbstractConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectiveConfig.class);

    private final Object configBean;
    private final Method getNamesMethod;
    private final Method getParamMethod;
    private final Method setParamMethod;
    private final Method getAbsolutePathMethod;

    public ReflectiveConfig(Object configBean, String getNamesMethodName, String getParamMethodName, String setParamMethodName,
            String getAbsolutePathMethodName) {
        try {
            this.configBean = configBean;
            this.getNamesMethod = configBean.getClass().getMethod(getNamesMethodName);
            this.getParamMethod = configBean.getClass().getMethod(getParamMethodName, String.class);
            this.setParamMethod = setParamMethodName != null ? configBean.getClass().getMethod(setParamMethodName, String.class, Object.class) : null;
            this.getAbsolutePathMethod = getAbsolutePathMethodName != null ? configBean.getClass().getMethod(getAbsolutePathMethodName, String.class)
                    : null;
        } catch (Exception e) {
            throw new ConfigException("Can't initialize config with bean " + configBean + ", getParam() method '" + getParamMethodName
                    + "', getAbsolutePath() method '" + getAbsolutePathMethodName + "'", e);
        }
    }

    @Override
    public boolean isEmpty() {
        return getNames().isEmpty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<String>();
        try {
            Object result = getNamesMethod.invoke(configBean);
            if (names instanceof Iterable) {
                Iterator<?> it = ((Iterable<?>) result).iterator();
                while (it.hasNext())
                    names.add(it.next().toString());
            } else if (result.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(result); i++)
                    names.add(Array.get(result, i).toString());
            } else {
                throw new UnsupportedOperationException("Unsupported result type " + result.getClass());
            }
            return names;
        } catch (Throwable e) {
            LOG.debug("Can't get parameter names with " + configBean + '.' + getNamesMethod.getName(), e);
            return null;
        }
    }

    @Override
    public String getParam(String name) {
        try {
            return getParamMethod.invoke(configBean, name).toString();
        } catch (Throwable e) {
            LOG.debug("Can't get parameter " + name + " with " + configBean + '.' + getParamMethod.getName(), e);
            return null;
        }
    }

    @Override
    public void setParam(String name, Object value) {
        if (setParamMethod == null)
            throw new UnsupportedOperationException();
        try {
            setParamMethod.invoke(configBean, name, value);
        } catch (Throwable e) {
            LOG.debug("Can't set parameter " + name + " = " + value + " with " + configBean + '.' + setParamMethod.getName(), e);
        }
    }

    @Override
    public File getAbsolutePath(String relativePath) {
        if (getAbsolutePathMethod == null)
            return super.getAbsolutePath(relativePath);
        try {
            return new File(getAbsolutePathMethod.invoke(configBean, relativePath).toString());
        } catch (Exception e) {
            throw new ConfigException("Can't get absolute path for relative path '" + relativePath + "'", e);
        }
    }

}
