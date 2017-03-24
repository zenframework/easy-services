package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URI;

import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.impl.ServiceMethodInterceptor;

public class TcpClientFactory extends ClientFactoryImpl {

    private final ThreadLocal<Socket> sockets = new ThreadLocal<Socket>();

    public TcpClientFactory() {}

    public TcpClientFactory(String baseUrl) {
        super(baseUrl);
    }

    @Override
    protected ServiceMethodInterceptor getMethodInterceptor(ServiceLocator serviceLocator, boolean useDescriptors) {
        return new TcpServiceMethodInterceptor(serviceLocator, useDescriptors, getSocket(serviceLocator));
    }

    private Socket getSocket(ServiceLocator serviceLocator) {
        try {
            URI uri = new URI(serviceLocator.getBaseUrl());
            Socket socket = sockets.get();
            if (socket == null || !socket.getInetAddress().getHostName().equals(uri.getHost()) || socket.getPort() != uri.getPort()) {
                socket = new Socket(uri.getHost(), uri.getPort());
                sockets.set(socket);
            }
            return socket;
            //return new Socket(uri.getHost(), uri.getPort());
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    private class TcpServiceMethodInterceptor extends ServiceMethodInterceptor {

        private final Socket socket;

        TcpServiceMethodInterceptor(ServiceLocator serviceLocator, boolean useDescriptors, Socket socket) {
            super(TcpClientFactory.this, serviceLocator, useDescriptors);
            this.socket = socket;
        }

        @Override
        protected ClientRequest createRequest(Method method, MethodDescriptor methodDescriptor) throws IOException {
            return new TcpClientRequest(clientFactory, socket, serviceLocator.getServiceName(), method, outParametersMode);
        }

    }

}
