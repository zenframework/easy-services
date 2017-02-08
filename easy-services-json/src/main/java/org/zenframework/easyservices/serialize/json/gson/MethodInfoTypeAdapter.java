package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zenframework.commons.cls.ClassRef;
import org.zenframework.commons.cls.MethodInfo;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class MethodInfoTypeAdapter extends TypeAdapter<MethodInfo> {

    private static final String NAME = "name";
    private static final String PARAMETER_TYPES = "parameterTypes";
    private static final String RETURN_TYPE = "returnType";

    public static final MethodInfoTypeAdapter INSTANCE = new MethodInfoTypeAdapter();

    @Override
    public void write(JsonWriter out, MethodInfo value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            out.name(NAME).value(value.getName());
            out.name(PARAMETER_TYPES);
            out.beginArray();
            for (ClassRef paramType : value.getParameterTypes())
                ClassRefTypeAdapter.INSTANCE.write(out, paramType);
            out.endArray();
            out.name(RETURN_TYPE);
            ClassRefTypeAdapter.INSTANCE.write(out, value.getReturnType());
            out.endObject();
        }
    }

    @Override
    public MethodInfo read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String methodName = null;
        List<ClassRef> paramTypes = new ArrayList<ClassRef>();
        ClassRef returnType = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (NAME.equals(name)) {
                methodName = in.nextString();
            } else if (PARAMETER_TYPES.equals(name)) {
                in.beginArray();
                while (in.hasNext())
                    paramTypes.add(ClassRefTypeAdapter.INSTANCE.read(in));
                in.endArray();
            } else if (RETURN_TYPE.equals(name)) {
                returnType = ClassRefTypeAdapter.INSTANCE.read(in);
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        return new MethodInfo(methodName, paramTypes, returnType);
    }

}
