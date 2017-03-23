package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.OutputStream;

import org.zenframework.easyservices.ServiceResponse;

public class TcpServiceResponse extends ServiceResponse {

    private final String sessionId;
    private final OutputStream out;

    public TcpServiceResponse(String sessionId, OutputStream out) {
        this.sessionId = sessionId;
        this.out = out;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public void sendSuccess() throws IOException {
        new TcpResponseHeader(sessionId, true).write(out);
    }

    @Override
    public void sendError(Throwable e) throws IOException {
        new TcpResponseHeader(sessionId, false).write(out);
    }

}
