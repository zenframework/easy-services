package org.zenframework.easyservices.resource.cache;

import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.resource.Resource;

public interface CacheItem extends Resource {

    boolean isWriteLocked();

    boolean isUpToDate(long resourceLastModified);

    void writeContent(InputStream in) throws IOException;

    void writeContent(byte[] content) throws IOException;

}
