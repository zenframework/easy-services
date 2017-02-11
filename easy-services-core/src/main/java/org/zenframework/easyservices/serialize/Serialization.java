package org.zenframework.easyservices.serialize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.commons.bean.ServiceUtil;

public class Serialization {

    private static volatile String defaultFormat = "json";
    private static final Map<String, SerializerFactory> factories = initFactories();

    private Serialization() {}

    public static void setDefaultFormat(String format) {
        defaultFormat = format;
    }

    public static String getDefaultFormat() {
        return defaultFormat;
    }

    public static SerializerFactory getDefaultFactory() {
        return factories.get(defaultFormat);
    }

    public static Map<String, SerializerFactory> getFactories() {
        return factories;
    }

    private static Map<String, SerializerFactory> initFactories() {
        Map<String, SerializerFactory> factories = new HashMap<String, SerializerFactory>();
        for (SerializerFactory factory : ServiceUtil.getServices(SerializerFactory.class))
            factories.put(factory.getFormat(), factory);
        return Collections.unmodifiableMap(factories);
    }

}
