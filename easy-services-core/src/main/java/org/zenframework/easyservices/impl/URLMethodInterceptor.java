package org.zenframework.easyservices.impl;

import java.io.IOException;

import org.zenframework.easyservices.ClientConnection;
import org.zenframework.easyservices.ServiceLocator;

public class URLMethodInterceptor extends ServiceMethodInterceptor {

    public URLMethodInterceptor(URLClientFactory clientFactory, ServiceLocator serviceLocator, boolean useDescriptors) {
        super(clientFactory, serviceLocator, useDescriptors);
    }

    @Override
    protected ClientConnection getServiceConnection(String methodName) throws IOException {
        return new URLClientConnection(clientFactory, serviceLocator, methodName);
    }

}
