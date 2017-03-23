package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.impl.ServiceMethodInterceptor;

public class TcpClientFactory extends ClientFactoryImpl {

    private final ThreadLocal<Socket> sockets = new ThreadLocal<Socket>();

    private URI baseUri;

    public TcpClientFactory() {}

    public TcpClientFactory(String baseUrl) {
        super(baseUrl);
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        super.setBaseUrl(baseUrl);
        try {
            baseUri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected ServiceMethodInterceptor getMethodInterceptor(ServiceLocator serviceLocator, boolean useDescriptors) {
        return new ServiceMethodInterceptor(this, serviceLocator, useDescriptors) {

            @Override
            protected ClientRequest createRequest(Method method, MethodDescriptor methodDescriptor) throws IOException {
                return new TcpClientRequest(clientFactory, getSocket(), serviceLocator.getServiceName(), method, outParametersMode);
            }

        };
    }

    private Socket getSocket() throws IOException {
        Socket socket = sockets.get();
        if (socket == null || !socket.getInetAddress().getHostName().equals(baseUri.getHost()) || socket.getPort() != baseUri.getPort()) {
            socket = new SharedSocket(baseUri.getHost(), baseUri.getPort());
            sockets.set(socket);
        }
        return socket;
    }

    private static class SharedSocket extends Socket {

        private final InputStream in = super.getInputStream();
        private final OutputStream out = super.getOutputStream();

        private SharedSocket(String host, int port) throws IOException {
            super(host, port);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return in;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return out;
        }

    }

}
