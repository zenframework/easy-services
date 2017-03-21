package org.zenframework.easyservices.socket;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class TcpClientRequest implements ClientRequest {

    private final ClientFactoryImpl clientFactory;
    private final ServiceLocator serviceLocator;
    private final String methodName;
    private final boolean outParametersMode;
    private final OutputStream out;
    private final InputStream in;
    private boolean success;

    public TcpClientRequest(ClientFactoryImpl clientFactory, ServiceLocator serviceLocator, String methodName, boolean outParametersMode)
            throws IOException {
        this.clientFactory = clientFactory;
        this.serviceLocator = serviceLocator;
        this.methodName = methodName;
        this.outParametersMode = outParametersMode;
        try {
            URI uri = new URI(serviceLocator.getBaseUrl());
            final Socket socket = new Socket(uri.getHost(), uri.getPort());
            out = new BlockOutputStream(socket.getOutputStream());
            in = new FilterInputStream(new BlockInputStream(socket.getInputStream())) {

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        socket.close();
                    }
                }

            };
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(serviceLocator.toString(), e);
        }
    }

    @Override
    public void writeRequestHeader() throws IOException {
        new RequestHeader(clientFactory.getSessionId(), serviceLocator.getServiceName(), methodName, outParametersMode).write(out);
    }

    @Override
    public void readResponseHeader() throws IOException {
        ResponseHeader header = new ResponseHeader();
        header.read(in);
        clientFactory.setSessionId(header.getSessionId());
        success = header.isSuccess();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return in;
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

}
