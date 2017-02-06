package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TypeTypeAdapter extends TypeAdapter<Class<?>> {

    public static final TypeTypeAdapter INSTANCE = new TypeTypeAdapter();

    @Override
    public void write(JsonWriter out, Class<?> value) throws IOException {
        out.value(value.getCanonicalName());
    }

    @Override
    public Class<?> read(JsonReader in) throws IOException {
        try {
            return Class.forName(in.nextString());
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
