package org.zenframework.easyservices.serialize.json;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ErrorDescription;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerAdapter;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.serialize.json.adapters.ListJsonSerializerAdapter;
import org.zenframework.easyservices.serialize.json.adapters.MapJsonSerializerAdapter;
import org.zenframework.easyservices.serialize.json.adapters.SetJsonSerializerAdapter;
import org.zenframework.easyservices.serialize.json.gson.DateStringTypeAdapter;
import org.zenframework.easyservices.serialize.json.gson.ErrorDescriptionJsonSerializer;
import org.zenframework.easyservices.serialize.json.gson.LocaleTypeAdapter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapterFactory;

public class JsonSerializerFactory implements SerializerFactory<JsonElement> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonSerializerFactory.class);

    private static final Map<Class<?>, SerializerAdapter<?>> SERIALIZER_ADAPTERS_DEFAULT = getDefaultSerializerAdapters();
    private static final Collection<TypeAdapterFactory> TYPE_ADAPTER_FACTORIES_DEFAULT = getDefaultTypeAdapterFactories();
    private static final Map<Type, Object> TYPE_ADAPTERS_DEFAULT = getDefaultTypeAdapters();

    private Map<Class<?>, SerializerAdapter<?>> serializerAdapters = SERIALIZER_ADAPTERS_DEFAULT;
    private Collection<TypeAdapterFactory> typeAdapterFactories = TYPE_ADAPTER_FACTORIES_DEFAULT;
    private Map<Type, Object> typeAdapters = TYPE_ADAPTERS_DEFAULT;
    private boolean exposedOnly = true;

    @Override
    public Serializer<JsonElement> getSerializer() {
        GsonBuilder builder = new GsonBuilder();
        if (exposedOnly)
            builder.excludeFieldsWithoutExposeAnnotation();
        for (TypeAdapterFactory factory : typeAdapterFactories)
            builder.registerTypeAdapterFactory(factory);
        for (Map.Entry<Type, Object> entry : typeAdapters.entrySet())
            builder.registerTypeAdapter(entry.getKey(), entry.getValue());
        return new JsonSerializer(builder.create());
    }

    @Override
    public SerializerAdapter<?> getAdapter(Class<?> type) {
        Map.Entry<Class<?>, SerializerAdapter<?>> candidate = null;
        for (Map.Entry<Class<?>, SerializerAdapter<?>> entry : serializerAdapters.entrySet()) {
            if (entry.getKey().isAssignableFrom(type) && (candidate == null || candidate.getKey().isAssignableFrom(entry.getKey())))
                candidate = entry;
        }
        return candidate != null ? candidate.getValue() : null;
    }

    public void setTypeAdapterFactories(Collection<TypeAdapterFactory> typeAdapterFactories) {
        this.typeAdapterFactories = typeAdapterFactories;
    }

    public void setTypeAdapters(Map<Type, Object> typeAdapters) {
        this.typeAdapters = typeAdapters;
    }

    public void setExposedOnly(boolean exposedOnly) {
        this.exposedOnly = exposedOnly;
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

    private static Map<Type, Object> getDefaultTypeAdapters() {
        Map<Type, Object> typeAdapters = new HashMap<Type, Object>();
        typeAdapters.put(Date.class, new DateStringTypeAdapter());
        typeAdapters.put(ErrorDescription.class, new ErrorDescriptionJsonSerializer());
        typeAdapters.put(Locale.class, new LocaleTypeAdapter());
        return typeAdapters;
    }

    private static Map<Class<?>, SerializerAdapter<?>> getDefaultSerializerAdapters() {
        Map<Class<?>, SerializerAdapter<?>> adapters = new HashMap<Class<?>, SerializerAdapter<?>>();
        adapters.put(Collection.class, new ListJsonSerializerAdapter());
        adapters.put(Map.class, new MapJsonSerializerAdapter());
        adapters.put(Set.class, new SetJsonSerializerAdapter());
        return adapters;
    }

}
