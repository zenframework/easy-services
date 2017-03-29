package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.util.io.BlockInputStream;

public class TcpServiceRequest extends ServiceRequest {

    private final TcpRequestHeader header;
    private final InputStream in;

    public TcpServiceRequest(ServiceSession session, TcpRequestHeader header, InputStream in) throws IOException {
        super(session);
        this.header = header;
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
    public Class<?>[] getParameterTypes() {
        return header.getParameterTypes();
    }

    @Override
    public boolean isOutParametersMode() {
        return header.isOutParametersMode();
    }

    @Override
    protected InputStream internalGetInputStream() throws IOException {
        return new BlockInputStream(in);
    }

}
