package org.zenframework.easyservices.resource;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.resource.cache.CacheItem;
import org.zenframework.easyservices.resource.cache.ResourceCache;
import org.zenframework.easyservices.resource.cache.file.FileResourceCache;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;
import org.zenframework.easyservices.util.debug.TimeChecker;

public class CachingResourceFactory implements ResourceFactory, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(CachingResourceFactory.class);

    public static final String PARAM_CACHE_ENABLED = "cache.enabled";
    public static final String PARAM_RESOURCE_CACHE = "cache";
    public static final String PARAM_RESOURCE_FACTORY = "resources";

    private static final boolean DEFAULT_CACHE_ENABLED = true;
    private static final Class<FileResourceCache> DEFAULT_RESOURCE_CACHE = FileResourceCache.class;

    private ResourceFactory factory;
    private ResourceCache cache;

    public ResourceFactory getFactory() {
        return factory;
    }

    public void setFactory(ResourceFactory factory) {
        this.factory = factory;
    }

    public ResourceCache getCache() {
        return cache;
    }

    public void setCache(ResourceCache cache) {
        this.cache = cache;
    }

    @Override
    public void init(Config config) {
        factory = config.getInstance(PARAM_RESOURCE_FACTORY);
        if (config.getParam(PARAM_CACHE_ENABLED, DEFAULT_CACHE_ENABLED))
            cache = config.getInstance(PARAM_RESOURCE_CACHE, DEFAULT_RESOURCE_CACHE);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(factory, cache);
    }

    @Override
    public Resource getResource(String path) throws IOException {
        Resource resource = factory.getResource(path);
        if (resource.exists()) {
            TimeChecker time = null;
            if (LOG.isDebugEnabled())
                time = new TimeChecker("GET " + path, LOG);
            if (cache != null) {
                CacheItem cacheItem = cache.getCacheItem(path);
                if (!cacheItem.isUpToDate(resource.lastModified()) && cache.getWriteLock(cacheItem)) {
                    try {
                        cacheItem.writeContent(resource.openStream());
                    } finally {
                        cache.releaseWriteLock(cacheItem);
                    }
                }
                resource = cacheItem;
            }
            if (time != null)
                time.printDifference("CACHED: " + (cache != null));
        }
        return resource;
    }

}
