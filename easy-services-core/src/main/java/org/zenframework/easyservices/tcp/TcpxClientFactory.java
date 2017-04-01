package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.impl.ServiceMethodInterceptor;
import org.zenframework.easyservices.net.NioTcpClient;
import org.zenframework.easyservices.net.TcpClient;

public class TcpxClientFactory extends ClientFactoryImpl {

    private final ThreadLocal<TcpClient> clients = new ThreadLocal<TcpClient>();

    public TcpxClientFactory() {}

    public TcpxClientFactory(String baseUrl) {
        super(baseUrl);
    }

    @Override
    protected ServiceMethodInterceptor getMethodInterceptor(ServiceLocator serviceLocator, boolean useDescriptors) {
        return new TcpServiceMethodInterceptor(serviceLocator, useDescriptors, getClient(serviceLocator));
    }

    private TcpClient getClient(ServiceLocator serviceLocator) {
        try {
            URI uri = new URI(serviceLocator.getBaseUrl());
            TcpClient client = clients.get();
            if (client == null || !client.getHost().equals(uri.getHost()) || client.getPort() != uri.getPort()) {
                client = new NioTcpClient(uri.getHost(), uri.getPort());
                clients.set(client);
            }
            return client;
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    private class TcpServiceMethodInterceptor extends ServiceMethodInterceptor {

        private final TcpClient client;

        TcpServiceMethodInterceptor(ServiceLocator serviceLocator, boolean useDescriptors, TcpClient client) {
            super(TcpxClientFactory.this, serviceLocator, useDescriptors);
            this.client = client;
        }

        @Override
        protected ClientRequest createRequest(Method method, MethodDescriptor methodDescriptor) throws IOException {
            return new TcpxClientRequest(clientFactory, client, serviceLocator.getServiceName(), method, outParametersMode);
        }

    }

}
