package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.Locale;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class LocaleTypeAdapter extends TypeAdapter<Locale> {

    @Override
    public void write(JsonWriter out, Locale value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public Locale read(JsonReader in) throws IOException {
        return Locale.forLanguageTag(in.nextString());
    }

}
