package org.zenframework.easyservices.serialize.json;

import org.zenframework.easyservices.serialize.AbstractSerializer;
import org.zenframework.easyservices.serialize.SerializationException;
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
            return null;
        JsonArray json = parser.parse(data).getAsJsonArray();
        JsonElement[] structure = new JsonElement[json.size()];
        for (int i = 0; i < structure.length; i++)
            structure[i] = json.get(i);
        return structure;
    }

    @Override
    public String compile(JsonElement structure) {
        return structure == null ? NULL_STR : structure.toString();
    }

    @Override
    public String compile(JsonElement[] array) {
        if (array == null)
            return NULL_STR;
        JsonArray json = new JsonArray();
        for (JsonElement e : array)
            json.add(e);
        return json.toString();
    }

    @Override
    public <T> T deserialize(JsonElement structure, Class<T> objType) throws SerializationException {
        return gson.fromJson(structure, objType);
    }

    @Override
    public JsonElement serialize(Object object) {
        if (object == null)
            return null;
        return gson.toJsonTree(object);
    }

    @Override
    public JsonElement[] newArray(int length) {
        return new JsonElement[length];
    }

    public Gson getGson() {
        return gson;
    }

}
