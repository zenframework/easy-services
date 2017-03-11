package org.zenframework.easyservices.cache;

import java.io.IOException;

public interface ResourceCache {

    CacheItem getCacheItem(String resourcePath);

    /**
     * Locks new resource cache item or waits for lock release.
     * Result <code>false</code> means that resource cache item has been just updated.
     * @param cacheItem - cache item
     * @return <code>true</code> if resource cache item was locked
     * @throws IOException 
     */
    boolean getWriteLock(CacheItem cacheItem) throws IOException;

    /**
     * Release write lock
     * @param resourcePath
     */
    void releaseWriteLock(CacheItem cacheItem);

}
