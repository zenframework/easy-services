package org.zenframework.easyservices.resource.cache.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.resource.cache.AbstractCacheItem;

public class FileCacheItem extends AbstractCacheItem {

    private final File cacheItemFile;

    FileCacheItem(String resourcePath, File cacheItemFile) {
        super(resourcePath);
        this.cacheItemFile = cacheItemFile;
    }

    public File getCacheItemFile() {
        return cacheItemFile;
    }

    @Override
    public long lastModified() {
        return cacheItemFile.lastModified();
    }

    @Override
    public boolean exists() {
        return cacheItemFile.exists();
    }

    @Override
    protected OutputStream getOutputStream() throws FileNotFoundException {
        cacheItemFile.getParentFile().mkdirs();
        return new FileOutputStream(cacheItemFile);
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(cacheItemFile);
    }

}
