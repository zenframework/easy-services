package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.Map;

import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;

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
            for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : value.getMethodDescriptors().entrySet()) {
                out.name(entry.getKey().toString());
                MethodDescriptorTypeAdapter.INSTANCE.write(out, entry.getValue());
            }
            /*if (value.getDebug())
                out.name("debug").value(true);
            if (value.getValueDescriptor() != null) {
                out.name("value");
                ValueDescriptorTypeAdapter.INSTANCE.write(out, value.getValueDescriptor());
            }*/
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
            try {
                MethodIdentifier methodId = MethodIdentifier.parse(in.nextName());
                MethodDescriptor methodDescriptor = MethodDescriptorTypeAdapter.INSTANCE.read(in);
                value.setMethodDescriptor(methodId, methodDescriptor);
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
            /*String name = in.nextName();
            if ("debug".equals(name)) {
                value.setDebug(in.nextBoolean());
            } else if ("value".equals(name)) {
                value.setValueDescriptor(ValueDescriptorTypeAdapter.INSTANCE.read(in));
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }*/
        }
        in.endObject();
        return value;
    }

}
