package org.zenframework.easyservices.serialize.json.gson;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonSyntaxException;

public class URITypeAdapter extends FilterTypeAdapter<URI> {

    @Override
    protected URI toObject(String str) throws JsonSyntaxException {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            throw new JsonSyntaxException("Can't convert JSON string '" + str + "' to URI", e);
        }
    }

    @Override
    protected String toString(URI object) {
        return object.toString();
    }

}
