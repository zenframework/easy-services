package org.zenframework.easyservices.cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;
import org.zenframework.easyservices.debug.TimeChecker;

public abstract class AbstractResourceCache implements ResourceCache, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractResourceCache.class);

    public static final String PARAM_TIMEOUT = "timeout";

    public static final int DEFAULT_TIMEOUT = 30000;

    private final Set<String> locks = new HashSet<String>();

    private int timeout;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void init(Config config) {
        timeout = config.getParam(PARAM_TIMEOUT, DEFAULT_TIMEOUT);
    }

    @Override
    public void destroy(Config config) {
        synchronized (locks) {
            locks.clear();
        }
    }

    @Override
    public boolean getWriteLock(CacheItem cacheItem) throws IOException {

        boolean debug = LOG.isDebugEnabled();
        int timeout = 0;
        Set<String> locksCopy = null;

        synchronized (locks) {
            ((AbstractCacheItem) cacheItem).setWriteLocked(locks.add(cacheItem.getPath()));
            if (cacheItem.isWriteLocked() && debug)
                locksCopy = new HashSet<String>(locks);
        }

        if (!cacheItem.isWriteLocked()) {
            boolean wait = true;
            TimeChecker time = null;
            if (debug)
                time = new TimeChecker("WAIT " + cacheItem.getPath(), LOG);
            while (wait) {
                if (timeout > this.getTimeout())
                    throw new IOException(cacheItem.getPath() + " waitting timeout");
                try {
                    Thread.sleep(100);
                    timeout += 100;
                } catch (InterruptedException e) {
                    throw new IOException(cacheItem.getPath() + " waitting interrupted");
                }
                synchronized (locks) {
                    wait = locks.contains(cacheItem.getPath());
                }
            }
            if (debug)
                time.printDifference();
        } else if (debug) {
            LOG.debug("Locks: " + locksCopy);
        }

        return cacheItem.isWriteLocked();

    }

    @Override
    public void releaseWriteLock(CacheItem cacheItem) {
        boolean debug = LOG.isDebugEnabled();
        boolean released;
        Set<String> locksCopy = null;
        synchronized (locks) {
            released = locks.remove(cacheItem.getPath());
            if (debug)
                locksCopy = new HashSet<String>(locks);
        }
        if (debug) {
            if (released)
                LOG.debug("RELEASE " + cacheItem.getPath());
            LOG.debug("Locks: " + locksCopy);
        }
        ((AbstractCacheItem) cacheItem).setWriteLocked(false);
    }

}
