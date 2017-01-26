package org.zenframework.easyservices.impl;

import java.lang.reflect.Proxy;

import org.zenframework.commons.bean.ServiceUtil;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.AnnotationServiceDescriptorFactory;
import org.zenframework.easyservices.descriptor.ServiceDescriptorFactory;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ClientFactoryImpl implements ClientFactory {

    private static final RequestMapper DEFAULT_REQUEST_MAPPER = new RequestMapperImpl();
    private static final ServiceDescriptorFactory DEFAULT_SERVICE_DESCRIPTOR_FACTORY = new AnnotationServiceDescriptorFactory();
    private static final SerializerFactory DEFAULT_SERIALIZER_FACTORY = ServiceUtil.getService(SerializerFactory.class);

    private ServiceDescriptorFactory serviceDescriptorFactory = DEFAULT_SERVICE_DESCRIPTOR_FACTORY;
    private RequestMapper requestMapper = DEFAULT_REQUEST_MAPPER;
    private SerializerFactory serializerFactory = DEFAULT_SERIALIZER_FACTORY;
    private String baseUrl;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl, SerializerFactory serializerFactory, RequestMapper requestMapper) {
        this.baseUrl = baseUrl;
        this.serializerFactory = serializerFactory;
        this.requestMapper = requestMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { serviceClass }, new ServiceInvocationHandler(
                ServiceLocator.qualified(baseUrl, serviceName), serviceClass, serviceDescriptorFactory, serializerFactory, requestMapper));
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setServiceDescriptorFactory(ServiceDescriptorFactory serviceDescriptorFactory) {
        this.serviceDescriptorFactory = serviceDescriptorFactory;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setRequestMapper(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

}
