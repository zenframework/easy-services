package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.ServiceSession;

public class TcpxServiceRequestHandler extends AbstractTcpServiceRequestHandler<TcpxRequestHeader, TcpxServiceRequest, TcpxServiceResponse> {

    @Override
    protected TcpxRequestHeader newHeader() {
        return new TcpxRequestHeader();
    }

    @Override
    protected String getSessionId(TcpxRequestHeader header) {
        return header.getSessionId();
    }

    @Override
    protected void setSessionId(TcpxRequestHeader header, String sessionId) {
        header.setSessionId(sessionId);
    }

    @Override
    protected TcpxServiceRequest newServiceRequest(ServiceSession session, InputStream in, TcpxRequestHeader header) throws IOException {
        return new TcpxServiceRequest(session, in, header);
    }

    @Override
    protected TcpxServiceResponse newServiceResponse(String sessionId, OutputStream out) {
        return new TcpxServiceResponse(sessionId, out);
    }

    @Override
    protected boolean isKeepConnection(TcpxRequestHeader header) {
        return header.isKeepConnection();
    }

}
