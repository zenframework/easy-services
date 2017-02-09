package org.zenframework.easyservices.serialize.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.json.gson.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonSerializer implements Serializer {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final String NULL_STR = "null";

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
        return gson.fromJson(new InputStreamReader(in),
                valueDescriptor != null ? GsonUtil.getParameterizedType(objType, valueDescriptor.getTypeParameters()) : objType);
    }

    @Override
    public Object[] deserialize(InputStream in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException {
        Object[] result = new Object[objTypes.length];
        JsonReader reader = new JsonReader(new InputStreamReader(in, CHARSET));
        reader.beginArray();
        for (int i = 0; reader.hasNext(); i++)
            result[i] = gson.fromJson(reader,
                    valueDescriptors[i] != null ? GsonUtil.getParameterizedType(objTypes[i], valueDescriptors[i].getTypeParameters()) : objTypes[i]);
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
    public <T> T deserialize(String data, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException {
        if (data == null || data.equals(NULL_STR))
            return null;
        return gson.fromJson(data, valueDescriptor != null ? GsonUtil.getParameterizedType(objType, valueDescriptor.getTypeParameters()) : objType);
    }

    @Override
    public Object[] deserialize(String data, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws SerializationException {
        if (data == null || data.equals(NULL_STR))
            return new Object[0];
        Object[] result = new Object[objTypes.length];
        JsonArray json = parser.parse(data).getAsJsonArray();
        if (json.size() != result.length)
            throw new SerializationException("Incorrect number of arguments: " + json.size() + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = gson.fromJson(json.get(i),
                    valueDescriptors[i] != null ? GsonUtil.getParameterizedType(objTypes[i], valueDescriptors[i].getTypeParameters()) : objTypes[i]);
        return result;
    }

    @Override
    public String serialize(Object object) {
        if (object == null)
            return NULL_STR;
        return gson.toJson(object);
    }

}
