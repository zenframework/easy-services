package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.net.TcpClient;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class TcpxClientRequest implements ClientRequest {

    private final ClientFactoryImpl clientFactory;
    private final String serviceName;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final boolean outParametersMode;
    private final OutputStream out;
    private final InputStream in;
    private boolean success;

    public TcpxClientRequest(ClientFactoryImpl clientFactory, TcpClient client, String serviceName, Method method, boolean outParametersMode)
            throws IOException {
        this.clientFactory = clientFactory;
        this.serviceName = serviceName;
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.outParametersMode = outParametersMode;
        out = client.getOutputStream();
        in = client.getInputStream();
    }

    @Override
    public void writeRequestHeader() throws IOException {
        new TcpxRequestHeader(clientFactory.getSessionId(), serviceName, methodName, parameterTypes, outParametersMode, true).write(out);
    }

    @Override
    public void readResponseHeader() throws IOException {
        TcpxResponseHeader header = new TcpxResponseHeader();
        header.read(in);
        clientFactory.setSessionId(header.getSessionId());
        success = header.isSuccess();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new BlockOutputStream(out);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new BlockInputStream(in);
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

}
