package org.zenframework.easyservices.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.zenframework.easyservices.ServiceResponse;

public class SocketServiceResponse extends ServiceResponse {

    private final OutputStream out;
    private final DataOutputStream data;

    public SocketServiceResponse(OutputStream out) {
        this.out = out;
        this.data = new DataOutputStream(out);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public void sendSuccess() throws IOException {
        data.writeBoolean(true);
        data.flush();
    }

    @Override
    public void sendError(Throwable e) throws IOException {
        data.writeBoolean(false);
        data.flush();
    }

}
