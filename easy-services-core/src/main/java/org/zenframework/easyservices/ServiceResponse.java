package org.zenframework.easyservices;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.zenframework.easyservices.util.io.BlockOutputStream;

public abstract class ServiceResponse {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final long id;

    public ServiceResponse() {
        this.id = COUNTER.incrementAndGet();
    }

    public long getId() {
        return id;
    }

    public OutputStream getOutputStream() throws IOException {
        return isCacheInputSafe() ? getInternalOutputStream() : new BlockOutputStream(getInternalOutputStream());
    }

    abstract public boolean isCacheInputSafe();

    abstract public void sendSuccess() throws IOException;

    abstract public void sendError(Throwable e) throws IOException;

    abstract protected OutputStream getInternalOutputStream() throws IOException;

}
