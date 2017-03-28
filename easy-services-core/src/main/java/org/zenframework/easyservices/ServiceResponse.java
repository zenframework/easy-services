package org.zenframework.easyservices;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ServiceResponse {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final long id;
    private OutputStream out;

    public ServiceResponse() {
        id = COUNTER.incrementAndGet();
    }

    public long getId() {
        return id;
    }

    public OutputStream getOutputStream() throws IOException {
        if (out == null)
            out = getInternalOutputStream();
        return out;
    }

    abstract public void sendSuccess() throws IOException;

    abstract public void sendError(Throwable e) throws IOException;

    abstract protected OutputStream getInternalOutputStream() throws IOException;

}
