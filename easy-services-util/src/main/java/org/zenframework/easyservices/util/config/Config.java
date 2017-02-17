package org.zenframework.easyservices.util.config;

import java.io.File;
import java.util.List;

public interface Config {

    String PROP_WORKING_DIRECTORY = "_workingDirectory_";

    boolean isEmpty();

    Config getSubConfig(String prefix);

    List<String> getNames();

    Object getParam(String name);

    Object getRequiredParam(String name);

    String getParam(String name, String defaultValue);

    int getParam(String name, int defaultValue);

    boolean getParam(String name, boolean defaultValue);

    String[] getParam(String name, String[] defaultValue);

    Class<?> getParam(String name, Class<?> defaultValue);

    void setParam(String name, Object value);

    File getAbsolutePath(String relativePath);

    Object getInstance(String name);

    Object getInstance(String name, Object defaultValue);

    Object getInstance(String name, Class<?> defaultValueClass);

    // Object getConfigurableInstance(String name);

    // Object getConfigurableInstance(String name, Class<? extends Configurable> defaultValueClass);

    void destroyInstances(Object... instances);

    String toString(boolean prettyPrint);

}
