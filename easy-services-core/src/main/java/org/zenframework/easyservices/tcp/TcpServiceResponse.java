package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.OutputStream;

import org.zenframework.easyservices.net.DefaultHeader;

public class TcpServiceResponse extends AbstractTcpServiceResponse<DefaultHeader> {

    public TcpServiceResponse(String sessionId, OutputStream out) {
        super(new DefaultHeader(), out);
        header.setField(TcpURLHandler.HEADER_SESSION_ID, sessionId);
    }

    @Override
    public void sendSuccess() throws IOException {
        header.setField(TcpURLHandler.HEADER_SUCCESSFUL, Boolean.TRUE);
    }

    @Override
    public void sendError(Throwable e) throws IOException {
        header.setField(TcpURLHandler.HEADER_SUCCESSFUL, Boolean.FALSE);
    }

}
