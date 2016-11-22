package org.zenframework.easyservices.serialize.json;

import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonSerializer implements Serializer<JsonElement> {

    private static final String NULL_STR = "null";

    private final JsonParser parser = new JsonParser();
    private final Gson gson;

    public JsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T deserialize(JsonElement objStruct, Class<T> objType) throws SerializationException {
        return gson.fromJson(objStruct, objType);
    }

    @Override
    public <T> T deserialize(String objStr, Class<T> objType) throws SerializationException {
        if (objStr == null || objStr.equals(NULL_STR))
            return null;
        try {
            return gson.fromJson(objStr, objType);
        } catch (JsonSyntaxException e) {
            throw new SerializationException("Can't parse given arguments", e);
        }
    }

    @Override
    public Object deserialize(String objStr, SerializerAdapter<JsonElement> adapter, Class<?>... typeParameters) throws SerializationException {
        if (objStr == null || objStr.equals(NULL_STR))
            return null;
        try {
            return adapter.deserialize(this, parser.parse(objStr), typeParameters);
        } catch (JsonSyntaxException e) {
            throw new SerializationException("Can't parse given arguments", e);
        }
    }

    @Override
    public Object[] deserialize(JsonElement arrStruct, Class<?>[] objTypes) throws SerializationException {
        JsonArray array = arrStruct.getAsJsonArray();
        Object[] result = new Object[array.size()];
        for (int i = 0; i < array.size(); i++)
            result[i] = gson.fromJson(array.get(i), objTypes[i]);
        return result;
    }

    @Override
    public Object[] deserialize(String arrStr, Class<?> objTypes[]) throws SerializationException {
        if (arrStr == null || arrStr.equals(NULL_STR))
            return new Object[0];
        Object[] result = new Object[objTypes.length];
        try {
            JsonArray array = toJsonArray(arrStr);
            if (array.size() != result.length)
                throw new SerializationException("Incorrect number of arguments: " + array.size() + ", expected: " + result.length);
            for (int i = 0; i < result.length; i++)
                result[i] = gson.fromJson(array.get(i), objTypes[i]);
            return result;
        } catch (JsonSyntaxException e) {
            throw new SerializationException("Can't parse given arguments", e);
        }
    }

    @Override
    public Object[] deserialize(String arrStr, SerializerAdapter<JsonElement>[] adapters, Class<?> typeParameters[][])
            throws SerializationException {
        if (arrStr == null || arrStr.equals(NULL_STR))
            return new Object[0];
        Object[] result = new Object[adapters.length];
        try {
            JsonArray array = toJsonArray(arrStr);
            if (array.size() != result.length)
                throw new SerializationException("Incorrect number of arguments: " + array.size() + ", expected: " + result.length);
            for (int i = 0; i < result.length; i++)
                result[i] = adapters[i].deserialize(this, array.get(i), typeParameters[i]);
            return result;
        } catch (JsonSyntaxException e) {
            throw new SerializationException("Can't parse given arguments", e);
        }
    }

    @Override
    public String serialize(Object object) {
        if (object == null)
            return NULL_STR;
        return gson.toJson(object);
    }

    @Override
    public String serialize(Object object, SerializerAdapter<JsonElement> adapter) {
        return gson.toJson(adapter.serialize(this, object));
    }

    public Gson getGson() {
        return gson;
    }

    private JsonArray toJsonArray(String argsStr) {
        JsonElement elem = parser.parse(argsStr);
        if (elem.isJsonArray())
            return elem.getAsJsonArray();
        JsonArray array = new JsonArray();
        array.add(elem);
        return array;
    }

}
