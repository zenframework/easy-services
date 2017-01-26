package org.zenframework.easyservices.serialize.json.adapters;

import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.json.JsonSerializer;

import com.google.gson.JsonElement;

public interface JsonSerializerAdapter<T> {

    T deserialize(JsonSerializer jsonSerializer, JsonElement json, Class<?>... typeParameters) throws SerializationException;

}
