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
    public JsonElement[] parseArray(String data) throws SerializationException {
        if (data == null || data.equals(NULL_STR))
            return new JsonElement[0];
        try {
            JsonArray jsonArray = getJsonArray(parser.parse(data));
            JsonElement[] array = new JsonElement[jsonArray.size()];
            for (int i = 0; i < array.length; i++)
                array[i] = jsonArray.get(i);
            return array;
        } catch (JsonSyntaxException e) {
            throw new SerializationException("Can't parse data", e);
        }
    }

    @Override
    public String compile(JsonElement objStruct) throws SerializationException {
        return objStruct.toString();
    }

    @Override
    public Object deserialize(JsonElement objStruct, Class<?> objType) throws SerializationException {
        return gson.fromJson(objStruct, objType);
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

    private static JsonArray getJsonArray(JsonElement json) {
        if (json.isJsonArray())
            return json.getAsJsonArray();
        JsonArray array = new JsonArray();
        array.add(json);
        return array;
    }

}
