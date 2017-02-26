package org.zenframework.easyservices.serialize.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;

import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.ValueTransfer;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ParamDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.json.gson.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonSerializer implements Serializer {

    private final JsonParser parser = new JsonParser();
    private final Class<?>[] paramTypes;
    private final Class<?> returnType;
    private final MethodDescriptor methodDescriptor;
    private final Gson gson;

    public JsonSerializer(Class<?>[] paramTypes, Class<?> returnType, MethodDescriptor methodDescriptor, Gson gson) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.methodDescriptor = methodDescriptor;
        this.gson = gson;
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getParser() {
        return parser;
    }

    @Override
    public Object deserializeResult(InputStream in, boolean success) throws IOException, SerializationException {
        return deserialize(new JsonReader(new InputStreamReader(in)), success, returnType,
                success && methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null);
    }

    @Override
    public Object[] deserializeParameters(InputStream in) throws IOException, SerializationException {
        return deserialize(new JsonReader(new InputStreamReader(in)), true, paramTypes,
                methodDescriptor != null ? methodDescriptor.getParameterDescriptors() : new ValueDescriptor[paramTypes.length]);
    }

    @Override
    public ResponseObject deserializeResponse(InputStream in, boolean success) throws IOException, SerializationException {
        ParamDescriptor[] paramDescriptors = methodDescriptor != null ? methodDescriptor.getParameterDescriptors()
                : new ParamDescriptor[paramTypes.length];
        ValueDescriptor returnDescriptor = methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
        try {
            ResponseObject responseObject = new ResponseObject();
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("result".equals(name)) {
                    responseObject.setResult(deserialize(reader, success, returnType, returnDescriptor));
                } else if ("parameters".equals(name)) {
                    responseObject.setParameters(deserialize(reader, success, paramTypes, paramDescriptors));
                } else {
                    throw new IOException("Unexpected name '" + name + "'");
                }
            }
            reader.endObject();
            return responseObject;
        } catch (JsonParseException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public void serialize(Object object, OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        gson.toJson(object, writer);
        writer.flush();
    }

    private Object deserialize(JsonReader in, boolean success, Class<?> objType, ValueDescriptor valueDescriptor)
            throws IOException, SerializationException {
        Type type = !success ? Throwable.class
                : valueDescriptor == null ? objType
                        : valueDescriptor.getTransfer() == ValueTransfer.REF ? ServiceLocator.class
                                : GsonUtil.getParameterizedType(objType, valueDescriptor.getTypeParameters());
        return gson.fromJson(in, type);
    }

    private Object[] deserialize(JsonReader in, boolean success, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors)
            throws IOException, SerializationException {
        if (objTypes.length != valueDescriptors.length)
            throw new SerializationException(
                    "objTypes.length == " + objTypes.length + " != " + valueDescriptors.length + " == valueDescriptors.length");
        if (objTypes.length == 0)
            return new Object[0];
        Object[] result = new Object[objTypes.length];
        in.beginArray();
        int count;
        for (count = 0; in.hasNext(); count++) {
            if (count >= result.length)
                throw new SerializationException("JSON array size > array of types size");
            try {
                result[count] = deserialize(in, success, objTypes[count], valueDescriptors[count]);
            } catch (JsonParseException e) {
                throw new SerializationException(e);
            }
        }
        in.endArray();
        if (count != objTypes.length)
            throw new SerializationException("result.length == " + count + " != " + objTypes.length + " == objTypes.length");
        return result;
    }

}
