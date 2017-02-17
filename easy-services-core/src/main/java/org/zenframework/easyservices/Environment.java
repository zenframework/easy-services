package org.zenframework.easyservices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.commons.bean.ServiceUtil;
import org.zenframework.commons.config.Config;
import org.zenframework.commons.config.MapConfig;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class Environment {

    public static final String PROPERTIES_PREFIX = "easyservices";

    public static final String PROP_DEBUG = "debug";
    public static final String PROP_AUTO_ALIASING = "autoAliasing";
    public static final String PROP_DUPLICATE_METHOD_NAMES_SAFE = "duplicateMethodNamesSafe";
    public static final String PROP_OUT_PARAMETERS_MODE = "outParametersMode";
    public static final String PROP_SERIALIZATION_FORMAT = "serialize.format";

    private static final Config CONFIG = new MapConfig(System.getProperties()).getSubConfig(PROPERTIES_PREFIX);
    private static final Map<String, SerializerFactory> SERIALIZER_FACTORIES = initFactories();

    private static final boolean DEFAULT_DEBUG = false;
    private static final boolean DEFAULT_AUTO_ALIASING = true;
    private static final boolean DEFAULT_DUPLICATE_METHOD_NAMES_SAFE = false;
    private static final boolean DEFAULT_OUT_PARAMETERS_MODE = true;

    private Environment() {}

    public static boolean isDebug() {
        return CONFIG.getParam(PROP_DEBUG, DEFAULT_DEBUG);
    }

    public static void setDebug(boolean debug) {
        CONFIG.setParam(PROP_DEBUG, debug);
    }

    public static boolean isAutoAliasing() {
        return CONFIG.getParam(PROP_AUTO_ALIASING, DEFAULT_AUTO_ALIASING);
    }

    public static void setAutoAliasing(boolean autoAliasing) {
        CONFIG.setParam(PROP_AUTO_ALIASING, autoAliasing);
    }

    public static boolean isDuplicateMethodNamesSafe() {
        return CONFIG.getParam(PROP_DUPLICATE_METHOD_NAMES_SAFE, DEFAULT_DUPLICATE_METHOD_NAMES_SAFE);
    }

    public static void setDuplicateMethodNamesSafe(boolean duplicateMethodNamesSafe) {
        CONFIG.setParam(PROP_DUPLICATE_METHOD_NAMES_SAFE, duplicateMethodNamesSafe);
    }

    public static boolean isOutParametersMode() {
        return CONFIG.getParam(PROP_OUT_PARAMETERS_MODE, DEFAULT_OUT_PARAMETERS_MODE);
    }

    public static void setOutParametersMode(boolean outParametersMode) {
        CONFIG.setParam(PROP_OUT_PARAMETERS_MODE, outParametersMode);
    }

    public static String getSerializationFormat() {
        return CONFIG.getParam(PROP_SERIALIZATION_FORMAT, (String) null);
    }

    public static void setDefaultFormat(String format) {
        CONFIG.setParam(PROP_SERIALIZATION_FORMAT, format);
    }

    public static SerializerFactory getSerializerFactory() {
        SerializerFactory factory = SERIALIZER_FACTORIES.get(getSerializationFormat());
        if (factory != null)
            return factory;
        if (SERIALIZER_FACTORIES.size() > 0)
            return SERIALIZER_FACTORIES.values().iterator().next();
        return null;
    }

    public static Map<String, SerializerFactory> getSerializationFactories() {
        return SERIALIZER_FACTORIES;
    }

    private static Map<String, SerializerFactory> initFactories() {
        Map<String, SerializerFactory> factories = new HashMap<String, SerializerFactory>();
        for (SerializerFactory factory : ServiceUtil.getServices(SerializerFactory.class))
            factories.put(factory.getFormat(), factory);
        return Collections.unmodifiableMap(factories);
    }

}
