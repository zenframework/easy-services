package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ParamDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class MethodDescriptorTypeAdapter extends TypeAdapter<MethodDescriptor> {

    public static final MethodDescriptorTypeAdapter INSTANCE = new MethodDescriptorTypeAdapter();

    @Override
    public void write(JsonWriter out, MethodDescriptor value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            if (value.isDebug())
                out.name("debug").value(true);
            if (value.isClose())
                out.name("close").value(true);
            if (value.getAlias() != null)
                out.name("alias").value(value.getAlias());
            out.name("parameters").beginArray();
            for (ValueDescriptor paramDescriptor : value.getParameterDescriptors())
                ValueDescriptorTypeAdapter.INSTANCE.write(out, paramDescriptor);
            out.endArray();
            if (value.getReturnDescriptor() != null) {
                out.name("return");
                ValueDescriptorTypeAdapter.INSTANCE.write(out, value.getReturnDescriptor());
            }
            out.endObject();
        }
    }

    @Override
    public MethodDescriptor read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        Boolean debug = null;
        Boolean close = null;
        String alias = null;
        Map<Integer, ParamDescriptor> paramDescriptors = new HashMap<Integer, ParamDescriptor>();
        ValueDescriptor returnDescriptor = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("debug".equals(name)) {
                debug = in.nextBoolean();
            } else if ("close".equals(name)) {
                close = in.nextBoolean();
            } else if ("alias".equals(name)) {
                alias = in.nextString();
            } else if ("parameters".equals(name)) {
                in.beginArray();
                int i = 0;
                while (in.hasNext())
                    paramDescriptors.put(i++, ParamDescriptorTypeAdapter.INSTANCE.read(in));
                in.endArray();
            } else if ("return".equals(name)) {
                returnDescriptor = ValueDescriptorTypeAdapter.INSTANCE.read(in);
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        MethodDescriptor value = new MethodDescriptor(paramDescriptors.size());
        value.setDebug(debug);
        value.setClose(close);
        value.setAlias(alias);
        value.setParameterDescriptorsMap(paramDescriptors);
        value.setReturnDescriptor(returnDescriptor);
        return value;
    }

}
