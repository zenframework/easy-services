package org.zenframework.easyservices.config;

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

    <T> T getInstance(String name);

    <T> T getInstance(String name, T defaultValue);

    <T> T getInstance(String name, Class<T> defaultValueClass);

    <T> List<T> getInstances(String prefix);

    void destroyInstances(Object... instances);

    String toString(boolean prettyPrint);

}
