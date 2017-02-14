package org.zenframework.easyservices.serialize.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.json.gson.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonSerializer implements Serializer {

    private static final byte[] NULL_STR = "null".getBytes();

    private final JsonParser parser = new JsonParser();
    private final Gson gson;

    public JsonSerializer(Gson gson) {
        this.gson = gson;
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getParser() {
        return parser;
    }

    @Override
    public Object deserialize(InputStream in, Class<?> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException {
        try {
            return gson.fromJson(new InputStreamReader(in),
                    valueDescriptor != null ? GsonUtil.getParameterizedType(objType, valueDescriptor.getTypeParameters()) : objType);
        } catch (JsonParseException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public ResponseObject deserialize(InputStream in, Class<?> returnType, ValueDescriptor returnDescriptor, Class<?>[] paramTypes,
            ValueDescriptor[] paramDescirptors) throws IOException, SerializationException {
        try {
            ResponseObject responseObject = new ResponseObject();
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("success".equals(name)) {
                    responseObject.setSuccess(reader.nextBoolean());
                } else if ("result".equals(name)) {
                    responseObject.setResult(gson.fromJson(reader,
                            returnDescriptor != null ? GsonUtil.getParameterizedType(returnType, returnDescriptor.getTypeParameters()) : returnType));
                } else if ("parameters".equals(name)) {
                    responseObject.setParameters(deserialize(reader, paramTypes, paramDescirptors));
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
    public Object[] deserialize(InputStream in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException {
        return deserialize(new JsonReader(new InputStreamReader(in)), objTypes, valueDescriptors);
    }

    @Override
    public void serialize(Object object, OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        gson.toJson(object, writer);
        writer.flush();
    }

    @Override
    public Object deserialize(byte[] data, Class<?> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException {
        if (data == null || data.equals(NULL_STR))
            return null;
        return deserialize(new ByteArrayInputStream(data), objType, valueDescriptor);
    }

    @Override
    public ResponseObject deserialize(byte[] data, Class<?> returnType, ValueDescriptor returnDescriptor, Class<?>[] paramTypes,
            ValueDescriptor[] paramDescirptors) throws SerializationException, IOException {
        if (data == null || data.equals(NULL_STR))
            return null;
        return deserialize(new ByteArrayInputStream(data), returnType, returnDescriptor, paramTypes, paramDescirptors);
    }

    @Override
    public Object[] deserialize(byte[] data, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws SerializationException, IOException {
        if (data == null || data.equals(NULL_STR))
            return new Object[0];
        return deserialize(new ByteArrayInputStream(data), objTypes, valueDescriptors);
    }

    @Override
    public byte[] serialize(Object object) {
        if (object == null)
            return NULL_STR;
        return gson.toJson(object).getBytes();
    }

    private Object[] deserialize(JsonReader in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException {
        if (objTypes.length != valueDescriptors.length)
            throw new SerializationException(
                    "objTypes.length == " + objTypes.length + " != " + valueDescriptors.length + " == valueDescriptors.length");
        Object[] result = new Object[objTypes.length];
        in.beginArray();
        for (int i = 0; in.hasNext(); i++) {
            if (i >= result.length)
                throw new SerializationException("JSON array size > array of types size");
            try {
                result[i] = gson.fromJson(in, valueDescriptors[i] != null
                        ? GsonUtil.getParameterizedType(objTypes[i], valueDescriptors[i].getTypeParameters()) : objTypes[i]);
            } catch (JsonParseException e) {
                throw new SerializationException(e);
            }
        }
        in.endArray();
        return result;
    }

}
