package org.zenframework.easyservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.zenframework.easyservices.util.io.BlockInputStream;

public abstract class ServiceRequest {

    private static final AtomicLong COUNTER = new AtomicLong(new Date().getTime());

    private final long id;
    private final ServiceSession session;
    private byte[] cachedData;

    public ServiceRequest(ServiceSession session) {
        this.id = COUNTER.incrementAndGet();
        this.session = session;
    }

    public long getId() {
        return id;
    }

    public ServiceSession getSession() {
        return session;
    }

    public void cacheInput() throws IOException {
        if (cachedData != null)
            return;
        InputStream in = getCacheInputSafe(internalGetInputStream());
        try {
            byte[] buf = new byte[8192];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                out.write(buf, 0, n);
            cachedData = out.toByteArray();
        } finally {
            in.close();
        }
    }

    public InputStream getInputStream() throws IOException {
        return cachedData != null ? new ByteArrayInputStream(cachedData) : getCacheInputSafe(internalGetInputStream());
    }

    @Override
    public String toString() {
        return new StringBuilder().append('#').append(Long.toHexString(id).substring(2)).append(':').append(session.getId()).append('/')
                .append(getServiceName()).append('.').append(getMethodName()).append("()").toString();
    }

    abstract public boolean isCacheInputSafe();

    abstract public String getServiceName();

    abstract public String getMethodName();

    abstract public Class<?>[] getParameterTypes();

    abstract public boolean isOutParametersMode();

    abstract protected InputStream internalGetInputStream() throws IOException;

    private InputStream getCacheInputSafe(InputStream in) {
        return isCacheInputSafe() ? in : new BlockInputStream(in);
    }

}
