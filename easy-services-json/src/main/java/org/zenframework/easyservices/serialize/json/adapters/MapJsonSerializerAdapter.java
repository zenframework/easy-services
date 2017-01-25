package org.zenframework.easyservices.serialize.json.adapters;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.json.JsonSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MapJsonSerializerAdapter extends JsonSerializerAdapter<Map<?, ?>> {

    @Override
    protected Map<?, ?> deserialize(JsonSerializer jsonSerializer, JsonElement parsedElement, Class<?>... typeParameters)
            throws SerializationException {
        if (typeParameters == null || typeParameters.length != 2)
            throw new SerializationException("Expected 2 type parameter, but got " + typeParameters);
        if (typeParameters[0] != String.class)
            throw new SerializationException("The first type parameter MUST be String, got " + typeParameters[0]);
        Map<String, Object> result = new HashMap<String, Object>();
        JsonObject object = parsedElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
            result.put(entry.getKey(), jsonSerializer.deserialize(entry.getValue(), typeParameters[1]));
        return result;
    }

}
