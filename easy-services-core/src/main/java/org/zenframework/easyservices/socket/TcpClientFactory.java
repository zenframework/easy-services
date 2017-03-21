package org.zenframework.easyservices.socket;

import java.io.IOException;

import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.impl.ClientFactoryImpl;
import org.zenframework.easyservices.impl.ServiceMethodInterceptor;

public class TcpClientFactory extends ClientFactoryImpl {

    public TcpClientFactory() {}

    public TcpClientFactory(String baseUrl) {
        super(baseUrl);
    }

    @Override
    protected ServiceMethodInterceptor getMethodInterceptor(ServiceLocator serviceLocator, boolean useDescriptors) {
        return new ServiceMethodInterceptor(this, serviceLocator, useDescriptors) {

            @Override
            protected ClientRequest createRequest(String methodName) throws IOException {
                return new TcpClientRequest(clientFactory, serviceLocator, methodName, outParametersMode);
            }

        };
    }

}
