package org.zenframework.easyservices;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ServiceResponse {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final long id;

    public ServiceResponse() {
        id = COUNTER.incrementAndGet();
    }

    public long getId() {
        return id;
    }

    abstract public OutputStream getOutputStream() throws IOException;

    abstract public void sendSuccess() throws IOException;

    abstract public void sendError(Throwable e) throws IOException;

}
