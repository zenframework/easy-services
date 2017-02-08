package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public abstract class FilterTypeAdapter<T> extends TypeAdapter<T> {

    private final List<Filter<T>> filters = new LinkedList<Filter<T>>();

    public void setFilters(List<? extends Filter<T>> filters) {
        this.filters.clear();
        this.filters.addAll(filters);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        for (Filter<T> filter : filters) {
            value = filter.onWrite(value);
        }
        if (value == null) {
            out.nullValue();
        } else {
            out.value(toString(value));
        }
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String json = in.nextString();
        T value = toObject(json);
        for (Filter<T> filter : filters) {
            value = filter.onRead(value);
        }
        return value;
    }

    protected abstract T toObject(String str) throws JsonSyntaxException;

    protected abstract String toString(T object);

    public static interface Filter<T> {

        T onRead(T value);

        T onWrite(T value);

    }

}
