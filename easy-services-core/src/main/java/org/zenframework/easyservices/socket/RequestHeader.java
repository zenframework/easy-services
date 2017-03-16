package org.zenframework.easyservices.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class RequestHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String serviceName;
    private final String methodName;
    private final boolean outParametersMode;

    public RequestHeader(String serviceName, String methodName, boolean outParametersMode) {
        super();
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.outParametersMode = outParametersMode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isOutParametersMode() {
        return outParametersMode;
    }

    public static RequestHeader readRequestHeader(InputStream in) throws IOException {
        try {
            return (RequestHeader) new ObjectInputStream(in).readObject();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

}
