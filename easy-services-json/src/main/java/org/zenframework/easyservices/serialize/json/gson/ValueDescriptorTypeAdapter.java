package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zenframework.easyservices.ValueTransfer;
import org.zenframework.easyservices.descriptor.ValueDescriptor;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ValueDescriptorTypeAdapter extends TypeAdapter<ValueDescriptor> {

    public static final ValueDescriptorTypeAdapter INSTANCE = new ValueDescriptorTypeAdapter();

    @Override
    public void write(JsonWriter out, ValueDescriptor value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            if (value.getTransfer() != null && value.getTransfer() != ValueTransfer.DEFAULT)
                out.name("transfer").value(value.getTransfer().name().toLowerCase());
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

    @Override
    public ValueDescriptor read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        ValueDescriptor value = new ValueDescriptor();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("transfer".equals(name)) {
                value.setTransfer(ValueTransfer.forName(in.nextString()));
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

}
