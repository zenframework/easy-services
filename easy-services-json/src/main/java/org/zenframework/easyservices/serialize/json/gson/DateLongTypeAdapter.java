package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class DateLongTypeAdapter extends TypeAdapter<Date> {

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        out.value(value.getTime());
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        return new Date(in.nextLong());
    }

}
