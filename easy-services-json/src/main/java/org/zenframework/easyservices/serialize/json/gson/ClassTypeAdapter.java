package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ClassTypeAdapter extends TypeAdapter<Class<?>> {

    public static final ClassTypeAdapter INSTANCE = new ClassTypeAdapter();

    @Override
    public void write(JsonWriter out, Class<?> value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getName());
        }
    }

    @Override
    public Class<?> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        try {
            return Class.forName(in.nextString());
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
