package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zenframework.easyservices.descriptor.ParamDescriptor;
import org.zenframework.easyservices.descriptor.ValueTransfer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ParamDescriptorTypeAdapter extends TypeAdapter<ParamDescriptor> {

    public static final ParamDescriptorTypeAdapter INSTANCE = new ParamDescriptorTypeAdapter();

    @Override
    public void write(JsonWriter out, ParamDescriptor value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            if (value.getTransfer() != null)
                out.name("transfer").value(value.getTransfer().name().toLowerCase());
            if (value.isClose())
                out.name("close").value(true);
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
    public ParamDescriptor read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        ParamDescriptor value = new ParamDescriptor();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("transfer".equals(name)) {
                value.setTransfer(ValueTransfer.forName(in.nextString()));
            } else if ("close".equals(name)) {
                value.setClose(in.nextBoolean());
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
