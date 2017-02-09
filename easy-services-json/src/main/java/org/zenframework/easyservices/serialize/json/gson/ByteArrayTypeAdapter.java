package org.zenframework.easyservices.serialize.json.gson;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ByteArrayTypeAdapter extends TypeAdapter<byte[]>{

    @Override
    public void write(JsonWriter out, byte[] value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(Base64.encodeBase64String(value));
        }
    }

    @Override
    public byte[] read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return Base64.decodeBase64(in.nextString());
    }

}
