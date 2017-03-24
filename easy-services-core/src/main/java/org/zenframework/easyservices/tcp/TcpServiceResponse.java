package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.OutputStream;

import org.zenframework.easyservices.ServiceResponse;

public class TcpServiceResponse extends ServiceResponse {

    private final String sessionId;
    private final OutputStream out;
    private final boolean cacheInputSafe;

    public TcpServiceResponse(String sessionId, OutputStream out, boolean cacheInputSafe) {
        this.sessionId = sessionId;
        this.out = out;
        this.cacheInputSafe = cacheInputSafe;
    }

    @Override
    public boolean isCacheInputSafe() {
        return cacheInputSafe;
    }

    @Override
    public void sendSuccess() throws IOException {
        new TcpResponseHeader(sessionId, true).write(out);
    }

    @Override
    public void sendError(Throwable e) throws IOException {
        new TcpResponseHeader(sessionId, false).write(out);
    }

    @Override
    protected OutputStream getInternalOutputStream() throws IOException {
        return out;
    }

}
