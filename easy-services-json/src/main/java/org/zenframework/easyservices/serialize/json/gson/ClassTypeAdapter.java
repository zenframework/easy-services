package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.zenframework.easyservices.ClassInfo;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ClassTypeAdapter extends TypeAdapter<Class<?>> {

    @Override
    public void write(JsonWriter out, Class<?> value) throws IOException {
        writeClass(out, value, new LinkedList<Class<?>>(), 0);
    }

    @Override
    public Class<?> read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
    }

    private static void writeClass(JsonWriter out, Class<?> clazz, Collection<Class<?>> serialized, int arrayDeep)
            throws IOException {
        if (clazz.isArray()) {
            writeClass(out, clazz.getComponentType(), serialized, arrayDeep + 1);
            return;
        }
        String mapped = ClassInfo.mapSimpleClass(clazz);
        if (mapped != null) {
            out.value(ClassInfo.toArray(arrayDeep, mapped));
        } else if (clazz.isEnum()) {
            out.beginObject();
            out.name(ClassInfo.TYPE);
            out.value(ClassInfo.toArray(arrayDeep, ClassInfo.ENUM, clazz.getCanonicalName()));
            out.name(ClassInfo.ENUM);
            out.beginArray();
            for (Object o : clazz.getEnumConstants()) {
                out.value(((Enum<?>) o).name());
            }
            out.endArray();
            out.endObject();
        } else {
            if (serialized.contains(clazz)) {
                out.value(ClassInfo.toArray(arrayDeep, ClassInfo.CLASS, clazz.getCanonicalName()));
            } else {
                serialized.add(clazz);
                out.beginObject();
                out.name(ClassInfo.TYPE);
                out.value(ClassInfo.toArray(arrayDeep, ClassInfo.CLASS, clazz.getCanonicalName()));
                out.name(ClassInfo.CLASS);
                out.beginObject();
                for (Entry<String, Class<?>> field : ClassInfo.getFields(clazz).entrySet()) {
                    out.name(field.getKey());
                    writeClass(out, field.getValue(), serialized, 0);
                }
                out.endObject();
                out.endObject();
            }
        }
    }

}
