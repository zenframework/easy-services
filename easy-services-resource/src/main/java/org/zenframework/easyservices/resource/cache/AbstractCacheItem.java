package org.zenframework.easyservices.resource.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public abstract class AbstractCacheItem implements CacheItem {

    private final String resourcePath;
    private boolean writeLocked = false;

    protected AbstractCacheItem(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public String getPath() {
        return resourcePath;
    }

    @Override
    public boolean isWriteLocked() {
        return writeLocked;
    }

    void setWriteLocked(boolean writeLocked) {
        this.writeLocked = writeLocked;
    }

    @Override
    public boolean isUpToDate(long resourceLastModified) {
        return exists() && lastModified() > resourceLastModified;
    }

    @Override
    public void writeContent(InputStream in) throws IOException {
        if (!writeLocked)
            throw new IOException("Cache item is not locked");
        OutputStream out = getOutputStream();
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in, out);
        }
    }

    @Override
    public void writeContent(byte[] content) throws IOException {
        if (!writeLocked)
            throw new IOException("Cache item is not locked");
        OutputStream out = getOutputStream();
        try {
            out.write(content);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    abstract protected OutputStream getOutputStream() throws IOException;

}
