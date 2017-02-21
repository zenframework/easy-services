package org.zenframework.easyservices.impl;

import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.CachingDescriptorFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.update.ValueUpdater;
import org.zenframework.easyservices.update.ValueUpdaterImpl;

public class ClientFactoryImpl implements ClientFactory {

    private DescriptorFactory descriptorFactory = new ClientDescriptorFactory();
    private SerializerFactory serializerFactory = Environment.getSerializerFactory();
    private boolean outParametersMode = Environment.isOutParametersMode();
    private ValueUpdater updater = ValueUpdaterImpl.INSTANCE;
    private boolean debug = Environment.isDebug();
    private String baseUrl;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return ClientProxy.getCGLibProxy(serviceClass, ServiceLocator.qualified(baseUrl, serviceName), descriptorFactory, serializerFactory,
                outParametersMode, updater, debug);
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

    public boolean isOutParametersSupport() {
        return outParametersMode;
    }

    public void setOutParametersSupport(boolean outParametersSupport) {
        this.outParametersMode = outParametersSupport;
    }

    public ValueUpdater getUpdater() {
        return updater;
    }

    public void setUpdater(ValueUpdater updater) {
        this.updater = updater;
    }

    public DescriptorFactory getDescriptorFactory() {
        return descriptorFactory;
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private class ClientDescriptorFactory extends CachingDescriptorFactory {

        private DescriptorFactory remoteDescriptorFactory = null;

        @Override
        protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
            return getRemoteFactory().getClassDescriptor(cls);
        }

        private synchronized DescriptorFactory getRemoteFactory() {
            if (remoteDescriptorFactory == null)
                remoteDescriptorFactory = ClientProxy.getCGLibProxy(DescriptorFactory.class,
                        ServiceLocator.qualified(baseUrl, DescriptorFactory.NAME), null, serializerFactory, outParametersMode, updater, debug);
            return remoteDescriptorFactory;
        }

    }

}
