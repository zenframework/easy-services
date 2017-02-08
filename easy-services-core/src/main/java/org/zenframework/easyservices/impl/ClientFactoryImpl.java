package org.zenframework.easyservices.impl;

import org.zenframework.commons.bean.ServiceUtil;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.AbstractClassDescriptorFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.ClassDescriptorFactory;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ClientFactoryImpl implements ClientFactory {

    private static final SerializerFactory DEFAULT_SERIALIZER_FACTORY = ServiceUtil.getService(SerializerFactory.class);

    private ClassDescriptorFactory classDescriptorFactory = new ClientClassDescriptorFactory();
    private RequestMapper requestMapper = RequestMapperImpl.INSTANCE;
    private SerializerFactory serializerFactory = DEFAULT_SERIALIZER_FACTORY;
    private String baseUrl;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl, SerializerFactory serializerFactory, RequestMapper requestMapper) {
        this.baseUrl = baseUrl;
        this.serializerFactory = serializerFactory;
        this.requestMapper = requestMapper;
    }

    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return ServiceInvocationHandler.getProxy(serviceClass, ServiceLocator.qualified(baseUrl, serviceName), classDescriptorFactory,
                serializerFactory, requestMapper);
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setClassDescriptorFactory(ClassDescriptorFactory classDescriptorFactory) {
        this.classDescriptorFactory = classDescriptorFactory;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setRequestMapper(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    private class ClientClassDescriptorFactory extends AbstractClassDescriptorFactory {

        private ClassDescriptorFactory remoteClassDescriptorFactory = null;

        @Override
        protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
            if (remoteClassDescriptorFactory == null)
                remoteClassDescriptorFactory = ServiceInvocationHandler.getProxy(ClassDescriptorFactory.class,
                        ServiceLocator.qualified(baseUrl, ClassDescriptorFactory.NAME), null, serializerFactory, requestMapper);
            return remoteClassDescriptorFactory.getClassDescriptor(cls);
        }

    }

}
