package org.zenframework.easyservices.serialize.json;

import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.json.adapters.JsonSerializerAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonSerializer implements Serializer {

    private static final String NULL_STR = "null";

    private final JsonParser parser = new JsonParser();
    private final JsonSerializerFactory factory;
    private final Gson gson;

    public JsonSerializer(JsonSerializerFactory factory, Gson gson) {
        this.factory = factory;
        this.gson = gson;
    }

    @Override
    public <T> T deserialize(String data, Class<T> objType) throws SerializationException {
        if (data == null || data.equals(NULL_STR))
            return null;
        return gson.fromJson(data, objType);
    }

    @Override
    public <T> T deserialize(String data, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException {
        if (data == null || data.equals(NULL_STR))
            return null;
        JsonSerializerAdapter<T> adapter = getJsonSerializerAdapter(objType, valueDescriptor);
        if (adapter != null)
            return adapter.deserialize(this, parser.parse(data), valueDescriptor != null ? valueDescriptor.getTypeParameters() : new Class<?>[0]);
        return deserialize(data, objType);
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
            result[i] = deserialize(json.get(i), objTypes[i], valueDescriptors[i]);
        return result;
    }

    @Override
    public String serialize(Object object) {
        if (object == null)
            return NULL_STR;
        return gson.toJson(object);
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getParser() {
        return parser;
    }

    @SuppressWarnings("unchecked")
    private <T> JsonSerializerAdapter<T> getJsonSerializerAdapter(Class<T> objType, ValueDescriptor valueDescriptor) {
        JsonSerializerAdapter<T> adapter = null;
        if (valueDescriptor != null) {
            adapter = valueDescriptor.getAdapter(JsonSerializerAdapter.class);
            if (adapter == null)
                adapter = factory.getAdapter(objType);
        }
        return adapter;
    }

    private <T> T deserialize(JsonElement json, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException {
        if (json == null)
            return null;
        JsonSerializerAdapter<T> adapter = getJsonSerializerAdapter(objType, valueDescriptor);
        if (adapter != null)
            return adapter.deserialize(this, json, valueDescriptor != null ? valueDescriptor.getTypeParameters() : new Class<?>[0]);
        return gson.fromJson(json, objType);
    }

}
