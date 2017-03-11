package org.zenframework.easyservices.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayResource implements Resource {

    private final String path;
    private final long lastModified;
    private final byte[] content;

    public ByteArrayResource(String path, long lastModified, byte[] content) {
        this.path = path;
        this.lastModified = lastModified;
        this.content = content;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean exists() {
        return lastModified > 0;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

}
