package org.zenframework.easyservices.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.net.Header;

public class TcpRequestHeader implements Header {

    private String sessionId;
    private String serviceName;
    private String methodName;
    private boolean outParametersMode;

    public TcpRequestHeader() {}

    public TcpRequestHeader(String sessionId, String serviceName, String methodName, boolean outParametersMode) {
        this.sessionId = sessionId;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.outParametersMode = outParametersMode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isOutParametersMode() {
        return outParametersMode;
    }

    public void setOutParametersMode(boolean outParametersMode) {
        this.outParametersMode = outParametersMode;
    }

    @Override
    public void read(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        sessionId = data.readUTF();
        serviceName = data.readUTF();
        methodName = data.readUTF();
        outParametersMode = data.readBoolean();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.writeUTF(sessionId != null ? sessionId : "");
        data.writeUTF(serviceName);
        data.writeUTF(methodName);
        data.writeBoolean(outParametersMode);
        data.flush();
    }

}
