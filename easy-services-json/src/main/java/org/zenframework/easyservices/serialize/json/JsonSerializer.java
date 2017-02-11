package org.zenframework.easyservices.serialize.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializationException;
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
    public <T> T deserialize(InputStream in, Class<T> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException {
        try {
            return gson.fromJson(new InputStreamReader(in),
                    valueDescriptor != null ? GsonUtil.getParameterizedType(objType, valueDescriptor.getTypeParameters()) : objType);
        } catch (JsonParseException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public Object[] deserialize(InputStream in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException {
        if (objTypes.length != valueDescriptors.length)
            throw new SerializationException(
                    "objTypes.length == " + objTypes.length + " != " + valueDescriptors.length + " == valueDescriptors.length");
        Object[] result = new Object[objTypes.length];
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        reader.beginArray();
        for (int i = 0; reader.hasNext(); i++) {
            if (i >= result.length)
                throw new SerializationException("JSON array size > array of types size");
            try {
                result[i] = gson.fromJson(reader, valueDescriptors[i] != null
                        ? GsonUtil.getParameterizedType(objTypes[i], valueDescriptors[i].getTypeParameters()) : objTypes[i]);
            } catch (JsonParseException e) {
                throw new SerializationException(e);
            }
        }
        reader.endArray();
        return result;
    }

    @Override
    public void serialize(Object object, OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        gson.toJson(object, writer);
        writer.flush();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException, IOException {
        if (data == null || data.equals(NULL_STR))
            return null;
        return deserialize(new ByteArrayInputStream(data), objType, valueDescriptor);
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

}
