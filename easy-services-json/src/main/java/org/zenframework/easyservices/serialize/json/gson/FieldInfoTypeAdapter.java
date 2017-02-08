package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import org.zenframework.commons.cls.ClassRef;
import org.zenframework.commons.cls.FieldInfo;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class FieldInfoTypeAdapter extends TypeAdapter<FieldInfo> {

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String READABLE = "readable";
    private static final String WRITABLE = "writable";

    public static final FieldInfoTypeAdapter INSTANCE = new FieldInfoTypeAdapter();

    @Override
    public void write(JsonWriter out, FieldInfo value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            out.name(NAME).value(value.getName());
            out.name(TYPE).value(value.getType().getName());
            out.name(READABLE).value(value.isReadable());
            out.name(WRITABLE).value(value.isWritable());
            out.endObject();
        }
    }

    @Override
    public FieldInfo read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String fieldName = null;
        ClassRef type = null;
        boolean readable = false, writable = false;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (NAME.equals(name)) {
                fieldName = in.nextString();
            } else if (TYPE.equals(name)) {
                type = ClassRefTypeAdapter.INSTANCE.read(in);
            } else if (READABLE.equals(name)) {
                readable = in.nextBoolean();
            } else if (WRITABLE.equals(name)) {
                writable = in.nextBoolean();
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        return new FieldInfo(fieldName, type, readable, writable);
    }

}
