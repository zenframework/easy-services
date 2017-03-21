package org.zenframework.easyservices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.MapConfig;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.util.bean.ServiceUtil;

public class Environment {

    private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

    public static final String PROPERTIES_PREFIX = "easyservices";

    public static final String PROP_DEBUG = "debug";
    public static final String PROP_AUTO_ALIASING = "autoAliasing";
    public static final String PROP_DUPLICATE_METHOD_NAMES_SAFE = "duplicateMethodNamesSafe";
    public static final String PROP_OUT_PARAMETERS_MODE = "outParametersMode";
    public static final String PROP_SECURITY_ENABLED = "securityEnabled";
    public static final String PROP_SERIALIZATION_FORMAT = "serializationFormat";

    private static final Config CONFIG = new MapConfig(System.getProperties()).getSubConfig(PROPERTIES_PREFIX);
    private static final Map<String, SerializerFactory> SERIALIZER_FACTORIES = initFactories();
    private static final Map<String, URLHandler> URL_HANDLERS = initURLHandlers();

    private static final boolean DEFAULT_DEBUG = true;
    private static final boolean DEFAULT_AUTO_ALIASING = false;
    private static final boolean DEFAULT_DUPLICATE_METHOD_NAMES_SAFE = true;
    private static final boolean DEFAULT_OUT_PARAMETERS_MODE = false;
    private static final boolean DEFAULT_SECURITY_ENABLED = false;

    private static final String PREFERRED_SERIALIZATION_FORMAT = "json";

    static {
        LOG.info("CONFIG: " + CONFIG.toString(true));
        StringBuffer str = new StringBuffer();
        str.append("DEFAULTS:");
        str.append("\n\t- debug:                    ").append(isDebug());
        str.append("\n\t- autoAliasing:             ").append(isAutoAliasing());
        str.append("\n\t- duplicateMethodNamesSafe: ").append(isDuplicateMethodNamesSafe());
        str.append("\n\t- outParametersMode:        ").append(isOutParametersMode());
        str.append("\n\t- securityEnabled:          ").append(isSecurityEnabled());
        str.append("\n\t- serializationFormat:      ").append(getSerializationFormat());
        LOG.info(str.toString());
    }

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

    public static boolean isSecurityEnabled() {
        return CONFIG.getParam(PROP_SECURITY_ENABLED, DEFAULT_SECURITY_ENABLED);
    }

    public static void setSecurityEnabled(boolean securityEnabled) {
        CONFIG.setParam(PROP_SECURITY_ENABLED, securityEnabled);
    }

    public static String getSerializationFormat() {
        String format = CONFIG.getParam(PROP_SERIALIZATION_FORMAT, (String) null);
        if (format != null && SERIALIZER_FACTORIES.containsKey(format))
            return format;
        if (SERIALIZER_FACTORIES.containsKey(PREFERRED_SERIALIZATION_FORMAT))
            return PREFERRED_SERIALIZATION_FORMAT;
        return SERIALIZER_FACTORIES.keySet().iterator().next();
    }

    public static void setSerializationFormat(String format) {
        CONFIG.setParam(PROP_SERIALIZATION_FORMAT, format);
    }

    public static SerializerFactory getSerializerFactory() {
        return SERIALIZER_FACTORIES.get(getSerializationFormat());
    }

    public static SerializerFactory getSerializerFactory(String format) {
        return SERIALIZER_FACTORIES.get(format);
    }

    public static Map<String, SerializerFactory> getSerializationFactories() {
        return SERIALIZER_FACTORIES;
    }

    public static URLHandler getURLHandler(String protocol) {
        return URL_HANDLERS.get(protocol);
    }

    public static Map<String, URLHandler> getURLHandlers() {
        return URL_HANDLERS;
    }

    private static Map<String, SerializerFactory> initFactories() {
        Map<String, SerializerFactory> factories = new HashMap<String, SerializerFactory>();
        for (SerializerFactory factory : ServiceUtil.getServices(SerializerFactory.class))
            factories.put(factory.getFormat(), factory);
        return Collections.unmodifiableMap(factories);
    }

    private static Map<String, URLHandler> initURLHandlers() {
        Map<String, URLHandler> handlers = new HashMap<String, URLHandler>();
        for (URLHandler handler : ServiceUtil.getServices(URLHandler.class))
            handlers.put(handler.getProtocol(), handler);
        return Collections.unmodifiableMap(handlers);
    }

}
