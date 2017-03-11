package org.zenframework.easyservices.cache.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.cache.AbstractResourceCache;
import org.zenframework.easyservices.cache.CacheItem;
import org.zenframework.easyservices.config.Config;

public class FileResourceCache extends AbstractResourceCache {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(FileResourceCache.class);

    public static final String PARAM_PATH = "path";

    private File cachePath;

    public File getCachePath() {
        return cachePath;
    }

    public void setCachePath(File cachePath) {
        this.cachePath = cachePath;
    }

    @Override
    public void init(Config config) {
        super.init(config);
        cachePath = config.getAbsolutePath(config.getRequiredParam(PARAM_PATH).toString());
    }

    @Override
    public CacheItem getCacheItem(String resourcePath) {
        return new FileCacheItem(resourcePath, new File(cachePath, resourcePath));
    }

}
