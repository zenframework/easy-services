package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.Locale;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocaleTypeAdapter extends TypeAdapter<Locale> {

    @Override
    public void write(JsonWriter out, Locale value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    @Override
    public Locale read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return Locale.forLanguageTag(in.nextString());
    }

}
