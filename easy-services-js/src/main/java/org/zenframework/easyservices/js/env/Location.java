package org.zenframework.easyservices.js.env;

import java.net.URI;
import java.net.URISyntaxException;

public class Location {

    public final String origin;
    public final String pathname;

    public Location(String uri) {
        try {
            URI u = new URI(uri);
            this.origin = u.getScheme() != null ? u.getScheme() + "://" + u.getAuthority() : null;
            this.pathname = u.getScheme() != null ? u.getPath() : uri;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
