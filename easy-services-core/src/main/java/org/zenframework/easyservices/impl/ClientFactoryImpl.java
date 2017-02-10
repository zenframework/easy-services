package org.zenframework.easyservices.impl;

import org.zenframework.commons.bean.ServiceUtil;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.AbstractClassDescriptorFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.ClassDescriptorFactory;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ClientFactoryImpl implements ClientFactory {

    private static final SerializerFactory DEFAULT_SERIALIZER_FACTORY = ServiceUtil.getService(SerializerFactory.class);

    private ClassDescriptorFactory classDescriptorFactory = new ClientClassDescriptorFactory();
    private SerializerFactory serializerFactory = DEFAULT_SERIALIZER_FACTORY;
    private String baseUrl;
    private boolean debug;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl, SerializerFactory serializerFactory) {
        this.baseUrl = baseUrl;
        this.serializerFactory = serializerFactory;
    }

    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return ClientProxy.getCGLibProxy(serviceClass, ServiceLocator.qualified(baseUrl, serviceName), classDescriptorFactory, serializerFactory,
                debug);
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

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private class ClientClassDescriptorFactory extends AbstractClassDescriptorFactory {

        private ClassDescriptorFactory remoteClassDescriptorFactory = null;

        @Override
        protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
            if (remoteClassDescriptorFactory == null)
                remoteClassDescriptorFactory = ClientProxy.getCGLibProxy(ClassDescriptorFactory.class,
                        ServiceLocator.qualified(baseUrl, ClassDescriptorFactory.NAME), null, serializerFactory, debug);
            return remoteClassDescriptorFactory.getClassDescriptor(cls);
        }

    }

}
