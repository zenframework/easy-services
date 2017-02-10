package org.zenframework.easyservices.serialize.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.CharSerializer;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.json.gson.GsonUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonSerializer implements CharSerializer {

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
    public <T> T deserialize(Reader in, Class<T> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException {
        return gson.fromJson(in, valueDescriptor != null ? GsonUtil.getParameterizedType(objType, valueDescriptor.getTypeParameters()) : objType);
    }

    @Override
    public Object[] deserialize(Reader in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException {
        Object[] result = new Object[objTypes.length];
        JsonReader reader = new JsonReader(in);
        reader.beginArray();
        for (int i = 0; reader.hasNext(); i++)
            result[i] = gson.fromJson(reader,
                    valueDescriptors[i] != null ? GsonUtil.getParameterizedType(objTypes[i], valueDescriptors[i].getTypeParameters()) : objTypes[i]);
        reader.endArray();
        return result;
    }

    @Override
    public void serialize(Object object, Writer out) throws IOException {
        gson.toJson(object, out);
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
        JsonElement json = parser.parse(data);
        if (!json.isJsonArray())
            throw new SerializationException("JSON array expected");
        JsonArray jsonArr = json.getAsJsonArray();
        if (jsonArr.size() != result.length)
            throw new SerializationException("Incorrect number of arguments: " + jsonArr.size() + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = gson.fromJson(jsonArr.get(i),
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
