package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.OutputStream;

public class TcpxServiceResponse extends AbstractTcpServiceResponse<TcpxResponseHeader> {

    public TcpxServiceResponse(String sessionId, OutputStream out) {
        super(new TcpxResponseHeader(sessionId), out);
    }

    @Override
    public void sendSuccess() throws IOException {
        header.setSuccess(true);
    }

    @Override
    public void sendError(Throwable e) throws IOException {
        header.setSuccess(false);
    }

}
