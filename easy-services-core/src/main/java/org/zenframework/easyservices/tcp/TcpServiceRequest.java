package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.net.DefaultHeader;

public class TcpServiceRequest extends AbstractTcpServiceRequest {

    private final DefaultHeader header;

    public TcpServiceRequest(ServiceSession session, InputStream in, DefaultHeader header) throws IOException {
        super(session, in);
        this.header = header;
    }

    @Override
    public String getServiceName() {
        return header.getString(DefaultHeader.PATH).substring(1);
    }

    @Override
    public String getMethodName() {
        return header.getString("method");
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return null;
    }

    @Override
    public boolean isOutParametersMode() {
        Boolean outParameters = header.getBoolean("outParameters");
        return outParameters != null && outParameters;
    }

}
