package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.util.io.BlockInputStream;

public abstract class AbstractTcpServiceRequest extends ServiceRequest {

    private final InputStream in;

    public AbstractTcpServiceRequest(ServiceSession session, InputStream in) throws IOException {
        super(session);
        this.in = in;
    }

    @Override
    protected InputStream internalGetInputStream() throws IOException {
        return new BlockInputStream(in);
    }

}
