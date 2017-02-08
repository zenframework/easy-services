package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.descriptor.ValueDescriptor;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ClassDescriptorTypeAdapter extends TypeAdapter<ClassDescriptor> {

    @Override
    public void write(JsonWriter out, ClassDescriptor value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            if (value.getValueDescriptor() != null) {
                out.name("value");
                write(out, value.getValueDescriptor());
            }
            if (!value.getMethodDescriptors().isEmpty()) {
                out.name("methods").beginObject();
                for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : value.getMethodDescriptors().entrySet()) {
                    out.name(entry.getKey().toString());
                    write(out, entry.getValue());
                }
                out.endObject();
            }
            out.endObject();
        }
    }

    @Override
    public ClassDescriptor read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        ClassDescriptor value = new ClassDescriptor();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("value".equals(name)) {
                value.setValueDescriptor(readValueDescriptor(in));
            } else if ("methods".equals(name)) {
                in.beginObject();
                while (in.hasNext()) {
                    try {
                        value.setMethodDescriptor(MethodIdentifier.parse(in.nextName()), readMethodDescriptor(in));
                    } catch (ClassNotFoundException e) {
                        throw new IOException(e);
                    }
                }
                in.endObject();
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        return value;
    }

    private static void write(JsonWriter out, ValueDescriptor value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            if (value.isReference())
                out.name("reference").value(true);
            if (!value.getAdapters().isEmpty()) {
                out.name("adapters").beginArray();
                for (Object adapter : value.getAdapters())
                    out.value(adapter.getClass().getCanonicalName());
                out.endArray();
            }
            if (value.getTypeParameters().length > 0) {
                out.name("typeParameters").beginArray();
                for (Class<?> typeParam : value.getTypeParameters())
                    out.value(typeParam.getCanonicalName());
                out.endArray();
            }
            out.endObject();
        }
    }

    private static ValueDescriptor readValueDescriptor(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        ValueDescriptor value = new ValueDescriptor();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("reference".equals(name)) {
                value.setReference(in.nextBoolean());
            } else if ("adapters".equals(name)) {
                in.beginArray();
                while (in.hasNext()) {
                    try {
                        value.addAdapter(Class.forName(in.nextString()).newInstance());
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                }
                in.endArray();
            } else if ("typeParameters".equals(name)) {
                List<Class<?>> typeParameters = new ArrayList<Class<?>>();
                in.beginArray();
                while (in.hasNext()) {
                    try {
                        typeParameters.add(Class.forName(in.nextString()));
                    } catch (ClassNotFoundException e) {
                        throw new IOException(e);
                    }
                }
                in.endArray();
                value.setTypeParameters(typeParameters.toArray(new Class[typeParameters.size()]));
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        return value;
    }

    private static void write(JsonWriter out, MethodDescriptor value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            if (value.getAlias() != null)
                out.name("alias").value(value.getAlias());
            out.name("parameters").beginArray();
            for (ValueDescriptor paramDescriptor : value.getParameterDescriptors())
                write(out, paramDescriptor);
            out.endArray();
            if (value.getReturnDescriptor() != null) {
                out.name("return");
                write(out, value.getReturnDescriptor());
            }
            out.endObject();
        }
    }

    private static MethodDescriptor readMethodDescriptor(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String alias = null;
        Map<Integer, ValueDescriptor> paramDescriptors = new HashMap<Integer, ValueDescriptor>();
        ValueDescriptor returnDescriptor = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("alias".equals(name)) {
                alias = in.nextString();
            } else if ("parameters".equals(name)) {
                in.beginArray();
                int i = 0;
                while (in.hasNext())
                    paramDescriptors.put(i++, readValueDescriptor(in));
                in.endArray();
            } else if ("return".equals(name)) {
                returnDescriptor = readValueDescriptor(in);
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        MethodDescriptor value = new MethodDescriptor(paramDescriptors.size());
        value.setAlias(alias);
        value.setParameterDescriptorsMap(paramDescriptors);
        value.setReturnDescriptor(returnDescriptor);
        return value;
    }

}
