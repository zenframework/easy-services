package org.zenframework.easyservices.serialize.json;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.serialize.json.gson.ByteArrayTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.ClassDescriptorTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.ClassInfoTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.ClassRefTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.ClassTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.DateStringTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.FieldInfoTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.LocaleTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.MethodDescriptorTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.MethodIdentifierTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.MethodInfoTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.ThrowableTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.URITypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.ValueDescriptorTypeAdapter;
import org.zenframework.easyservices.util.cls.ClassInfo;
import org.zenframework.easyservices.util.cls.ClassRef;
import org.zenframework.easyservices.util.cls.FieldInfo;
import org.zenframework.easyservices.util.cls.MethodInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class JsonSerializerFactory implements SerializerFactory {

    public static final String FORMAT = "json";

    private static final Logger LOG = LoggerFactory.getLogger(JsonSerializerFactory.class);

    private final Collection<TypeAdapterFactory> typeAdapterFactories = getDefaultTypeAdapterFactories();
    private final Map<Class<?>, TypeAdapter<?>> typeAdapters = getDefaultTypeAdapters();
    private boolean exposedOnly = false;

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public Serializer getSerializer(Class<?>[] paramTypes, Class<?> returnType, MethodDescriptor methodDescriptor) {
        //throw new UnsupportedOperationException();
        GsonBuilder builder = new GsonBuilder();
        if (exposedOnly)
            builder.excludeFieldsWithoutExposeAnnotation();
        builder.registerTypeAdapterFactory(new SimpleTypeAdapterFactory());
        for (TypeAdapterFactory factory : typeAdapterFactories)
            builder.registerTypeAdapterFactory(factory);
        return new JsonSerializer(paramTypes, returnType, methodDescriptor, builder.create());
    }

    /*@Override
    public JsonSerializer getCharSerializer() {
        GsonBuilder builder = new GsonBuilder();
        if (exposedOnly)
            builder.excludeFieldsWithoutExposeAnnotation();
        builder.registerTypeAdapterFactory(new SimpleTypeAdapterFactory());
        for (TypeAdapterFactory factory : typeAdapterFactories)
            builder.registerTypeAdapterFactory(factory);
        return new JsonSerializer(builder.create());
    }*/

    public void setTypeAdapterFactories(Collection<TypeAdapterFactory> typeAdapterFactories) {
        this.typeAdapterFactories.addAll(typeAdapterFactories);
    }

    public void setTypeAdapters(Map<Class<?>, TypeAdapter<?>> typeAdapters) {
        this.typeAdapters.putAll(typeAdapters);
    }

    public void setExposedOnly(boolean exposedOnly) {
        this.exposedOnly = exposedOnly;
    }

    private class SimpleTypeAdapterFactory implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Map.Entry<Class<?>, TypeAdapter<?>> candidate = null;
            for (Map.Entry<Class<?>, TypeAdapter<?>> entry : typeAdapters.entrySet()) {
                if (entry.getKey().isAssignableFrom(type.getRawType()) && (candidate == null || candidate.getKey().isAssignableFrom(entry.getKey())))
                    candidate = entry;
            }
            return candidate != null ? (TypeAdapter<T>) candidate.getValue() : null;
        }

    }

    private static Collection<TypeAdapterFactory> getDefaultTypeAdapterFactories() {
        Collection<TypeAdapterFactory> factories = new LinkedList<TypeAdapterFactory>();
        ServiceLoader<TypeAdapterFactory> factoriesLoader = ServiceLoader.load(TypeAdapterFactory.class);
        Iterator<TypeAdapterFactory> i = factoriesLoader.iterator();
        while (i.hasNext()) {
            TypeAdapterFactory factory = i.next();
            factories.add(factory);
            LOG.info("GSON type adapter factory " + factory.getClass() + " loaded");
        }
        return factories;
    }

    private static Map<Class<?>, TypeAdapter<?>> getDefaultTypeAdapters() {
        Map<Class<?>, TypeAdapter<?>> typeAdapters = new HashMap<Class<?>, TypeAdapter<?>>();
        typeAdapters.put(byte[].class, new ByteArrayTypeAdapter());
        typeAdapters.put(Throwable.class, new ThrowableTypeAdapter());
        typeAdapters.put(Date.class, new DateStringTypeAdapter());
        typeAdapters.put(Locale.class, new LocaleTypeAdapter());
        typeAdapters.put(Class.class, new ClassTypeAdapter());
        typeAdapters.put(URI.class, new URITypeAdapter());
        typeAdapters.put(ClassInfo.class, new ClassInfoTypeAdapter());
        typeAdapters.put(ClassRef.class, new ClassRefTypeAdapter());
        typeAdapters.put(FieldInfo.class, new FieldInfoTypeAdapter());
        typeAdapters.put(MethodInfo.class, new MethodInfoTypeAdapter());
        typeAdapters.put(ValueDescriptor.class, new ValueDescriptorTypeAdapter());
        typeAdapters.put(MethodIdentifier.class, new MethodIdentifierTypeAdapter());
        typeAdapters.put(MethodDescriptor.class, new MethodDescriptorTypeAdapter());
        typeAdapters.put(ClassDescriptor.class, new ClassDescriptorTypeAdapter());
        return typeAdapters;
    }

}
