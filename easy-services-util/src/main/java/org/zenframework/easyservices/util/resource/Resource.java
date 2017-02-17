package org.zenframework.easyservices.util.resource;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {

    String getPath();

    boolean exists();

    long lastModified();

    InputStream openStream() throws IOException;

}
