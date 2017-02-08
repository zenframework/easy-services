package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ThrowableTypeAdapter extends TypeAdapter<Throwable> {

    @Override
    public void write(JsonWriter out, Throwable value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            out.name("class").value(value.getClass().getName());
            out.name("message").value(value.getMessage());
            out.endObject();
        }
    }

    @Override
    public Throwable read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        Class<?> cls = null;
        String message = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("class".equals(name)) {
                try {
                    cls = Class.forName(in.nextString());
                } catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
            } else if ("message".equals(name)) {
                message = in.nextString();
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        return newInstance(cls, message);
    }

    private static Throwable newInstance(Class<?> cls, String message) throws IOException {
        try {
            return (Throwable) cls.getConstructor(String.class).newInstance(message);
        } catch (Throwable e) {
            try {
                return (Throwable) cls.getConstructor(String.class, Throwable.class).newInstance(message, null);
            } catch (Throwable e1) {
                try {
                    return (Throwable) cls.newInstance();
                } catch (Throwable e2) {
                    throw new IOException("Can't instantiate " + cls.getCanonicalName());
                }
            }
        }
    }

}
