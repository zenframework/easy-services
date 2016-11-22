package org.zenframework.easyservices.serialize.json.gson;

import java.lang.reflect.Type;

import org.zenframework.easyservices.ErrorDescription;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ErrorDescriptionJsonSerializer implements JsonSerializer<ErrorDescription> {

    @Override
    public JsonElement serialize(ErrorDescription src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.add("timestamp", context.serialize(src.getTimestamp()));
        json.add("className", new JsonPrimitive(src.getClassName()));
        json.add("message", new JsonPrimitive(src.getMessage() != null ? src.getMessage() : ""));
        return json;
    }

}
