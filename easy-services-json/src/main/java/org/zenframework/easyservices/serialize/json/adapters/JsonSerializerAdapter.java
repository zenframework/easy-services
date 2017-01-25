package org.zenframework.easyservices.serialize.json.adapters;

import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerAdapter;
import org.zenframework.easyservices.serialize.json.JsonSerializer;

import com.google.gson.JsonElement;

public abstract class JsonSerializerAdapter<T> implements SerializerAdapter<JsonElement, T> {

    @Override
    public JsonElement serialize(Serializer<JsonElement> serializer, Object object) {
        return serialize((JsonSerializer) serializer, object);
    }

    @Override
    public T deserialize(Serializer<JsonElement> serializer, JsonElement parsedElement, Class<?>... typeParameters) throws SerializationException {
        return deserialize((JsonSerializer) serializer, parsedElement, typeParameters);
    }

    protected JsonElement serialize(JsonSerializer jsonSerializer, Object object) {
        return jsonSerializer.getGson().toJsonTree(object);
    }

    protected abstract T deserialize(JsonSerializer jsonSerializer, JsonElement parsedElement, Class<?>... typeParameters)
            throws SerializationException;

}
