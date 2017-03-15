package org.zenframework.easyservices.impl;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.ClientURLHandler;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.CachingDescriptorFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.update.ValueUpdater;
import org.zenframework.easyservices.update.ValueUpdaterImpl;
import org.zenframework.easyservices.util.bean.ServiceUtil;

import net.sf.cglib.proxy.Enhancer;

public class ClientFactoryImpl implements ClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ClientFactoryImpl.class);

    private DescriptorFactory descriptorFactory = new ClientDescriptorFactory();
    private SerializerFactory serializerFactory = Environment.getSerializerFactory();
    private boolean outParametersMode = Environment.isOutParametersMode();
    private ValueUpdater updater = ValueUpdaterImpl.INSTANCE;
    private boolean debug = Environment.isDebug();
    private String baseUrl;
    private ClientURLHandler clientUrlHandler;
    private String sessionId;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return getProxy(serviceClass, serviceName, descriptorFactory);
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory) {
        this.descriptorFactory = descriptorFactory;
    }

    public DescriptorFactory getDescriptorFactory() {
        return descriptorFactory;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setOutParametersMode(boolean outParametersMode) {
        this.outParametersMode = outParametersMode;
    }

    public boolean isOutParametersMode() {
        return outParametersMode;
    }

    public void setUpdater(ValueUpdater updater) {
        this.updater = updater;
    }

    public ValueUpdater getUpdater() {
        return updater;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ClientURLHandler getClientUrlHandler() {
        if (clientUrlHandler == null) {
            try {
                URI baseUri = new URI(baseUrl);
                for (ClientURLHandler handler : ServiceUtil.getServices(ClientURLHandler.class))
                    if (handler.getProtocol().equals(baseUri.getScheme()))
                        clientUrlHandler = handler;
            } catch (Throwable e) {
                LOG.warn("Can't initialize default client URL handler for URL " + baseUrl, e);
            }
        }
        return clientUrlHandler;
    }

    public void setClientUrlHandler(ClientURLHandler clientUrlHandler) {
        this.clientUrlHandler = clientUrlHandler;
    }

    protected String getSessionId() {
        return sessionId;
    }

    protected void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @SuppressWarnings("unchecked")
    private <T> T getProxy(Class<T> serviceClass, String serviceName, DescriptorFactory classDescriptorFactory) {
        return (T) Enhancer.create(serviceClass,
                new ServiceMethodInterceptor(this, ServiceLocator.qualified(baseUrl, serviceName), classDescriptorFactory));
    }

    private class ClientDescriptorFactory extends CachingDescriptorFactory {

        private DescriptorFactory remoteDescriptorFactory = null;

        @Override
        protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
            return getRemoteFactory().getClassDescriptor(cls);
        }

        private synchronized DescriptorFactory getRemoteFactory() {
            if (remoteDescriptorFactory == null)
                remoteDescriptorFactory = getProxy(DescriptorFactory.class, DescriptorFactory.NAME, null);
            return remoteDescriptorFactory;
        }

    }

}
