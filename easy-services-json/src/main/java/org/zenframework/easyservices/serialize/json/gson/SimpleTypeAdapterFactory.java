package org.zenframework.easyservices.serialize.json.gson;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class SimpleTypeAdapterFactory implements TypeAdapterFactory {

    protected final Map<Class<?>, TypeAdapter<?>> typeAdapters = new HashMap<Class<?>, TypeAdapter<?>>();

    public SimpleTypeAdapterFactory() {}

    public SimpleTypeAdapterFactory(Map<Class<?>, TypeAdapter<?>> typeAdapters) {
        setTypeAdapters(typeAdapters);
    }

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

    public Map<Class<?>, TypeAdapter<?>> getTypeAdapters() {
        return typeAdapters;
    }

    public void setTypeAdapters(Map<Class<?>, TypeAdapter<?>> typeAdapters) {
        this.typeAdapters.clear();
        this.typeAdapters.putAll(typeAdapters);
    }

}
