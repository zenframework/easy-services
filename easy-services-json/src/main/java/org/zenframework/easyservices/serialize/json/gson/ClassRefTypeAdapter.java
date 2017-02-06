package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import org.zenframework.commons.cls.ClassRef;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ClassRefTypeAdapter extends TypeAdapter<ClassRef> {

    public static final ClassRefTypeAdapter INSTANCE = new ClassRefTypeAdapter();

    @Override
    public void write(JsonWriter out, ClassRef value) throws IOException {
        out.value(value.getName());
    }

    @Override
    public ClassRef read(JsonReader in) throws IOException {
        return new ClassRef(in.nextString());
    }

}
