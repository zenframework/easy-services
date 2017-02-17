package org.zenframework.easyservices.util.cache.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.zenframework.easyservices.util.cache.AbstractCacheItem;

public class MemoryCacheItem extends AbstractCacheItem {

    private long lastModified = -1;
    private byte[] content = null;

    MemoryCacheItem(String resourcePath) {
        super(resourcePath);
    }

    @Override
    public boolean exists() {
        return content != null;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return new InternalOutputStream();
    }

    private class InternalOutputStream extends ByteArrayOutputStream {

        @Override
        public void close() throws IOException {
            super.close();
            lastModified = new Date().getTime();
            content = toByteArray();
        }

    }

}
