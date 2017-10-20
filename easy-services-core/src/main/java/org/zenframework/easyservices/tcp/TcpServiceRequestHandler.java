package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.net.DefaultHeader;

public class TcpServiceRequestHandler extends AbstractTcpServiceRequestHandler<DefaultHeader, TcpServiceRequest, TcpServiceResponse> {

    @Override
    protected DefaultHeader newHeader() {
        return new DefaultHeader();
    }

    @Override
    protected String getSessionId(DefaultHeader header) {
        return header.getString(TcpURLHandler.HEADER_SESSION_ID);
    }

    @Override
    protected void setSessionId(DefaultHeader header, String sessionId) {
        header.setField(TcpURLHandler.HEADER_SESSION_ID, sessionId);
    }

    @Override
    protected TcpServiceRequest newServiceRequest(ServiceSession session, InputStream in, DefaultHeader header) throws IOException {
        return new TcpServiceRequest(session, in, header);
    }

    @Override
    protected TcpServiceResponse newServiceResponse(String sessionId, OutputStream out) {
        return new TcpServiceResponse(sessionId, out);
    }

    @Override
    protected boolean isKeepConnection(DefaultHeader header) {
        return false;
    }

}
