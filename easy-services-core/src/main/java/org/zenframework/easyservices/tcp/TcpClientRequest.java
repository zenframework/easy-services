package org.zenframework.easyservices.tcp;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class TcpClientRequest implements ClientRequest {

    private final ClientFactoryImpl clientFactory;
    private final String serviceName;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final boolean outParametersMode;
    private final OutputStream out;
    private final InputStream in;
    private boolean success;

    public TcpClientRequest(ClientFactoryImpl clientFactory, Socket socket, String serviceName, Method method, boolean outParametersMode)
            throws IOException {
        this.clientFactory = clientFactory;
        this.serviceName = serviceName;
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.outParametersMode = outParametersMode;
        out = socket.getOutputStream();
        in = socket.getInputStream();
    }

    @Override
    public void writeRequestHeader() throws IOException {
        new TcpRequestHeader(clientFactory.getSessionId(), serviceName, methodName, parameterTypes, outParametersMode).write(out);
    }

    @Override
    public void readResponseHeader() throws IOException {
        TcpResponseHeader header = new TcpResponseHeader();
        header.read(in);
        clientFactory.setSessionId(header.getSessionId());
        success = header.isSuccess();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return clientFactory.isCacheInputSafe() ? new FilterOutputStream(out) {

            @Override
            public void close() throws IOException {}

        } : new BlockOutputStream(out);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return clientFactory.isCacheInputSafe() ? new FilterInputStream(in) {

            @Override
            public void close() throws IOException {}

        } : new BlockInputStream(in);
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

}
