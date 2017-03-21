package org.zenframework.easyservices.socket;

import java.io.IOException;
import java.io.OutputStream;

import org.zenframework.easyservices.ServiceResponse;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class SocketServiceResponse extends ServiceResponse {

    private final String sessionId;
    private final OutputStream out;

    public SocketServiceResponse(String sessionId, OutputStream out) {
        this.sessionId = sessionId;
        this.out = new BlockOutputStream(out);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public void sendSuccess() throws IOException {
        new ResponseHeader(sessionId, true).write(out);
    }

    @Override
    public void sendError(Throwable e) throws IOException {
        new ResponseHeader(sessionId, false).write(out);
    }

}
