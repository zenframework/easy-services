package org.zenframework.easyservices.util.cache;

import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.util.resource.Resource;

public interface CacheItem extends Resource {

    boolean isWriteLocked();

    boolean isUpToDate(long resourceLastModified);

    void writeContent(InputStream in) throws IOException;

    void writeContent(byte[] content) throws IOException;

}
