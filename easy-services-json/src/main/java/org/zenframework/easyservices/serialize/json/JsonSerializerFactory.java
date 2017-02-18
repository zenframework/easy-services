package org.zenframework.easyservices.serialize.json;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.serialize.json.gson.DefaultTypeAdapterFactory;
import org.zenframework.easyservices.serialize.json.gson.SimpleTypeAdapterFactory;
import org.zenframework.easyservices.util.bean.ServiceUtil;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;

public class JsonSerializerFactory implements SerializerFactory {

    public static final String FORMAT = "json";

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(JsonSerializerFactory.class);

    private final Collection<TypeAdapterFactory> typeAdapterFactories = ServiceUtil.getServices(TypeAdapterFactory.class);
    private final SimpleTypeAdapterFactory defaultFactory = new DefaultTypeAdapterFactory();
    private boolean exposedOnly = false;

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public Serializer getSerializer(Class<?>[] paramTypes, Class<?> returnType, MethodDescriptor methodDescriptor) {
        GsonBuilder builder = new GsonBuilder();
        if (exposedOnly)
            builder.excludeFieldsWithoutExposeAnnotation();
        builder.registerTypeAdapterFactory(defaultFactory);
        for (TypeAdapterFactory factory : typeAdapterFactories)
            builder.registerTypeAdapterFactory(factory);
        return new JsonSerializer(paramTypes, returnType, methodDescriptor, builder.create());
    }

    public void setTypeAdapterFactories(Collection<TypeAdapterFactory> typeAdapterFactories) {
        this.typeAdapterFactories.addAll(typeAdapterFactories);
    }

    public Map<Class<?>, TypeAdapter<?>> getTypeAdapters() {
        return defaultFactory.getTypeAdapters();
    }

    public void setTypeAdapters(Map<Class<?>, TypeAdapter<?>> typeAdapters) {
        defaultFactory.setTypeAdapters(typeAdapters);
    }

    public void setExposedOnly(boolean exposedOnly) {
        this.exposedOnly = exposedOnly;
    }

}
