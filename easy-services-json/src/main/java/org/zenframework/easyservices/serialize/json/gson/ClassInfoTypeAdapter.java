package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import org.zenframework.commons.cls.ClassInfo;
import org.zenframework.commons.cls.FieldInfo;
import org.zenframework.commons.cls.MethodInfo;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ClassInfoTypeAdapter extends TypeAdapter<ClassInfo> {

    private static final String NAME = "name";
    private static final String FIELDS = "fields";
    private static final String METHODS = "methods";

    @Override
    public void write(JsonWriter out, ClassInfo value) throws IOException {
        out.beginObject();
        out.name(NAME).value(value.getName());
        out.name(FIELDS);
        out.beginObject();
        for (FieldInfo field : value.getFields().values()) {
            out.name(field.getName());
            FieldInfoTypeAdapter.INSTANCE.write(out, field);
        }
        out.endObject();
        out.name(METHODS);
        out.beginArray();
        for (MethodInfo method : value.getMethods()) {
            MethodInfoTypeAdapter.INSTANCE.write(out, method);
        }
        out.endArray();
        out.endObject();
    }

    @Override
    public ClassInfo read(JsonReader in) throws IOException {
        /*String className = null;
        Map<String, FieldInfo> fields = new HashMap<String, FieldInfo>();
        Set<MethodInfo> methods = new HashSet<MethodInfo>();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (NAME.equals(name)) {
                className = in.nextString();
            } else if (FIELDS.equals(name)) {
                in.beginObject();
                while (in.hasNext())
                    fields.put(in.nextName(), FieldInfoTypeAdapter.INSTANCE.read(in));
                in.endObject();
            } else if (METHODS.equals(name)) {
                in.beginArray();
                while (in.hasNext())
                    methods.add(MethodInfoTypeAdapter.INSTANCE.read(in));
                in.endArray();
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        return new ClassInfo(className, fields, methods);*/
        throw new UnsupportedOperationException();
    }

}
