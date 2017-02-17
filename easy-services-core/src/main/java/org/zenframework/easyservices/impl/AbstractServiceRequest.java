package org.zenframework.easyservices.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.ServiceRequest;

public abstract class AbstractServiceRequest implements ServiceRequest {

    private byte[] cachedData;

    @Override
    public void cacheInput() throws IOException {
        if (cachedData != null)
            return;
        InputStream in = internalGetInputStream();
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

    @Override
    public InputStream getInputStream() throws IOException {
        return cachedData != null ? new ByteArrayInputStream(cachedData) : internalGetInputStream();
    }

    abstract protected InputStream internalGetInputStream() throws IOException;

}
