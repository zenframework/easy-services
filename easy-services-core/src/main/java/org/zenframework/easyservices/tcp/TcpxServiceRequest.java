package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.ServiceSession;

public class TcpxServiceRequest extends AbstractTcpServiceRequest {

    private final TcpxRequestHeader header;

    public TcpxServiceRequest(ServiceSession session, InputStream in, TcpxRequestHeader header) throws IOException {
        super(session, in);
        this.header = header;
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

}
