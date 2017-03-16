package org.zenframework.easyservices.socket;

import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.ServiceSession;

public class SocketServiceRequest extends ServiceRequest {

    private final RequestHeader header;
    private final InputStream in;

    public SocketServiceRequest(ServiceSession session, InputStream in) throws IOException {
        super(session);
        this.header = RequestHeader.readRequestHeader(in);
        this.in = in;
    }

    @Override
    public String getServiceName() {
        return header.getServiceName();
    }

    @Override
    public String getMethodName() {
        return header.getMethodName();
    }

    @Override
    public boolean isOutParametersMode() {
        return header.isOutParametersMode();
    }

    @Override
    protected InputStream internalGetInputStream() throws IOException {
        return in;
    }

}
