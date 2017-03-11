package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import org.zenframework.easyservices.cls.ClassRef;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ClassRefTypeAdapter extends TypeAdapter<ClassRef> {

    public static final ClassRefTypeAdapter INSTANCE = new ClassRefTypeAdapter();

    @Override
    public void write(JsonWriter out, ClassRef value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getName());
        }
    }

    @Override
    public ClassRef read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            return new ClassRef(in.nextString());
        }
    }

}
