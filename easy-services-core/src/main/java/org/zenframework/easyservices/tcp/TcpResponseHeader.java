package org.zenframework.easyservices.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.net.Header;

public class TcpResponseHeader implements Header {

    private String sessionId;
    private boolean success;

    public TcpResponseHeader() {}

    public TcpResponseHeader(String sessionId, boolean success) {
        this.sessionId = sessionId;
        this.success = success;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public void read(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        sessionId = data.readUTF();
        success = data.readBoolean();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.writeUTF(sessionId);
        data.writeBoolean(success);
        data.flush();
    }

}
