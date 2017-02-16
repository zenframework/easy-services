package org.zenframework.easyservices.impl;

import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.CachingDescriptorFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.update.ValueUpdater;

public class ClientFactoryImpl implements ClientFactory {

    private DescriptorFactory descriptorFactory = new ClientDescriptorFactory();
    private SerializerFactory serializerFactory = Environment.getSerializerFactory();
    private ValueUpdater updater = new ValueUpdaterImpl();
    private boolean debug = Environment.isDebug();
    private String baseUrl;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl, SerializerFactory serializerFactory) {
        this.baseUrl = baseUrl;
        this.serializerFactory = serializerFactory;
    }

    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return ClientProxy.getCGLibProxy(serviceClass, ServiceLocator.qualified(baseUrl, serviceName), descriptorFactory, serializerFactory,
                updater, debug);
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory) {
        this.descriptorFactory = descriptorFactory;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private class ClientDescriptorFactory extends CachingDescriptorFactory {

        private DescriptorFactory remoteDescriptorFactory = null;

        @Override
        protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
            return getRemoteFactory().getClassDescriptor(cls);
        }

        @Override
        protected MethodDescriptor extractMethodDescriptor(MethodIdentifier methodId) {
            return getRemoteFactory().getMethodDescriptor(methodId);
        }

        private synchronized DescriptorFactory getRemoteFactory() {
            if (remoteDescriptorFactory == null)
                remoteDescriptorFactory = ClientProxy.getCGLibProxy(DescriptorFactory.class,
                        ServiceLocator.qualified(baseUrl, DescriptorFactory.NAME), null, serializerFactory, updater, debug);
            return remoteDescriptorFactory;
        }

    }

}
