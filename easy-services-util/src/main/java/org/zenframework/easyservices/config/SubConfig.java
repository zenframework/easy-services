package org.zenframework.easyservices.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SubConfig extends AbstractConfig {

    private final AbstractConfig parentConfig;
    private final String prefix;

    public SubConfig(AbstractConfig parentConfig, String prefix) {
        this.parentConfig = parentConfig;
        this.prefix = normalizePrefix(prefix);
    }

    @Override
    public boolean isEmpty() {
        for (String name : parentConfig.getNames())
            if (name.startsWith(prefix))
                return false;
        return true;
    }

    @Override
    public List<String> getNames() {
        List<String> list = new ArrayList<String>();
        for (String name : parentConfig.getNames())
            if (name.startsWith(prefix))
                list.add(name.substring(prefix.length()));
        return list;
    }

    @Override
    public Object getParam(String name) {
        return parentConfig.getParam(prefix + name);
    }

    @Override
    public void setParam(String name, Object value) {
        parentConfig.setParam(prefix + name, value);
    }

    @Override
    public File getAbsolutePath(String relativePath) {
        return parentConfig.getAbsolutePath(relativePath);
    }

    @Override
    protected Config getRootConfig() {
        return parentConfig.getRootConfig();
    }

    @Override
    protected Object getCachedInstance(String name) {
        return parentConfig.getCachedInstance(name);
    }

    @Override
    protected void cacheInstance(String name, Object instance) {
        parentConfig.cacheInstance(name, instance);
    }

    private static String normalizePrefix(String prefix) {
        return prefix.endsWith(".") ? prefix : prefix + '.';
    }

}
