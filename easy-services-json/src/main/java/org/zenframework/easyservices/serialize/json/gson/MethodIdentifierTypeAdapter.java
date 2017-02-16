package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import org.zenframework.easyservices.descriptor.MethodIdentifier;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class MethodIdentifierTypeAdapter extends TypeAdapter<MethodIdentifier> {

    @Override
    public void write(JsonWriter out, MethodIdentifier value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public MethodIdentifier read(JsonReader in) throws IOException {
        try {
            return MethodIdentifier.parse(in.nextString());
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
