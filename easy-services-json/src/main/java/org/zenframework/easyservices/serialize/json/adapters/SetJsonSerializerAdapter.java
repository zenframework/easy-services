package org.zenframework.easyservices.serialize.json.adapters;

import java.util.HashSet;
import java.util.Set;

import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.json.JsonSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class SetJsonSerializerAdapter extends JsonSerializerAdapter {

    @Override
    protected Object deserialize(JsonSerializer jsonSerializer, JsonElement parsedElement, Class<?>... typeParameters)
            throws SerializationException {
        if (typeParameters == null || typeParameters.length != 1)
            throw new SerializationException("Expected 1 type parameter, but got " + typeParameters);
        Set<Object> result = new HashSet<Object>();
        JsonArray array = parsedElement.getAsJsonArray();
        for (int i = 0; i < array.size(); i++)
            result.add(jsonSerializer.deserialize(array.get(i), typeParameters[0]));
        return result;
    }

}
