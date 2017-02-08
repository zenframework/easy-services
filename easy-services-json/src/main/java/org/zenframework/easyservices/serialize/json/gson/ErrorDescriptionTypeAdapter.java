package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import org.zenframework.easyservices.ErrorDescription;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ErrorDescriptionTypeAdapter extends TypeAdapter<ErrorDescription> {

    @Override
    public void write(JsonWriter out, ErrorDescription value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.beginObject();
            out.name("timestamp").value(value.getTimestamp());
            out.name("className").value(value.getClassName());
            out.name("message").value(value.getMessage() != null ? value.getMessage() : "");
            out.endObject();
        }
    }

    @Override
    public ErrorDescription read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String className = null;
        String message = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("class".equals(name)) {
                className = in.nextString();
            } else if ("message".equals(name)) {
                message = in.nextString();
            } else {
                throw new IOException("Unexpected name '" + name + "'");
            }
        }
        in.endObject();
        return new ErrorDescription(className, message);
    }

}
