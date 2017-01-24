package org.zenframework.easyservices.serialize.json;

import org.zenframework.easyservices.serialize.AbstractSerializer;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.SerializerAdapter;
import org.zenframework.easyservices.serialize.SerializerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonSerializer extends AbstractSerializer<JsonElement> {

    private static final String NULL_STR = "null";

    private final JsonParser parser = new JsonParser();

    private final Gson gson;

    public JsonSerializer(SerializerFactory<JsonElement> factory, Gson gson) {
        super(factory);
        this.gson = gson;
    }

    @Override
    public JsonElement parse(String data) throws SerializationException {
        if (data == null || data.equals(NULL_STR))
            return null;
        try {
            return parser.parse(data);
        } catch (JsonSyntaxException e) {
            throw new SerializationException("Can't parse data", e);
        }
    }

    @Override
    public String compile(JsonElement structure) {
        return structure == null ? NULL_STR : structure.toString();
    }

    @Override
    public Object deserialize(JsonElement objStruct, Class<?> objType) throws SerializationException {
        return gson.fromJson(objStruct, objType);
    }

    @Override
    public JsonElement serialize(Object object) {
        if (object == null)
            return null;
        return gson.toJsonTree(object);
    }

    @Override
    public JsonElement serialize(Object[] array) {
        if (array == null)
            return new JsonArray();
        return gson.toJsonTree(array);
    }

    @Override
    public JsonElement serialize(Object[] array, SerializerAdapter<JsonElement>[] adapters) {
        if (array == null)
            return serialize(array);
        JsonArray json = new JsonArray();
        for (int i = 0; i < array.length; i++)
            json.add(serialize(array[i], adapters[i]));
        return json;
    }

    public Gson getGson() {
        return gson;
    }

    @Override
    protected JsonElement[] toArray(JsonElement json) {
        if (json.isJsonArray()) {
            JsonArray jsonArr = json.getAsJsonArray();
            JsonElement[] arr = new JsonElement[jsonArr.size()];
            for (int i = 0; i < arr.length; i++)
                arr[i] = jsonArr.get(i);
            return arr;
        }
        return new JsonElement[] { json };
    }

}
