package org.zenframework.easyservices.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.ClassUtils;
import org.zenframework.easyservices.net.Header;

public class TcpRequestHeader implements Header {

    private String sessionId;
    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private boolean outParametersMode;
    private boolean keepConnection;

    public TcpRequestHeader() {}

    public TcpRequestHeader(String sessionId, String serviceName, String methodName, Class<?>[] parameterTypes, boolean outParametersMode,
            boolean keepConnection) {
        this.sessionId = sessionId;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.outParametersMode = outParametersMode;
        this.keepConnection = keepConnection;
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

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public boolean isOutParametersMode() {
        return outParametersMode;
    }

    public void setOutParametersMode(boolean outParametersMode) {
        this.outParametersMode = outParametersMode;
    }

    public boolean isKeepConnection() {
        return keepConnection;
    }

    public void setKeepConnection(boolean keepConnection) {
        this.keepConnection = keepConnection;
    }

    @Override
    public void read(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        sessionId = data.readUTF();
        serviceName = data.readUTF();
        methodName = data.readUTF();
        int paramsCount = data.readInt();
        if (paramsCount >= 0) {
            parameterTypes = new Class<?>[paramsCount];
            try {
                for (int i = 0; i < paramsCount; i++)
                    parameterTypes[i] = ClassUtils.getClass(data.readUTF());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        outParametersMode = data.readBoolean();
        keepConnection = data.readBoolean();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.writeUTF(sessionId != null ? sessionId : "");
        data.writeUTF(serviceName);
        data.writeUTF(methodName);
        if (parameterTypes == null) {
            data.writeInt(-1);
        } else {
            data.writeInt(parameterTypes.length);
            for (Class<?> parameterType : parameterTypes)
                data.writeUTF(parameterType.getName());
        }
        data.writeBoolean(outParametersMode);
        data.writeBoolean(keepConnection);
        data.flush();
    }

}
