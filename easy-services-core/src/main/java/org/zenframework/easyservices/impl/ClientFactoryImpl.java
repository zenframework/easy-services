package org.zenframework.easyservices.impl;

import java.lang.reflect.Proxy;

import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.ClientFactory;

public class ClientFactoryImpl implements ClientFactory {

    private SerializerFactory<?> serializerFactory;
    private RequestMapper requestMapper;
    private String baseUrl;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl, SerializerFactory<?> serializerFactory, RequestMapper requestMapper) {
        this.baseUrl = baseUrl;
        this.serializerFactory = serializerFactory;
        this.requestMapper = requestMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { serviceClass },
                new ServiceInvocationHandler(baseUrl + serviceName, serializerFactory, requestMapper));
    }

    public void setSerializerFactory(SerializerFactory<?> serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setRequestMapper(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
