package org.zenframework.easyservices.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.URLHandler;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.CachingDescriptorFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.update.ValueUpdater;
import org.zenframework.easyservices.update.ValueUpdaterImpl;

import net.sf.cglib.proxy.Enhancer;

public class ClientFactoryImpl implements ClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ClientFactoryImpl.class);

    private DescriptorFactory descriptorFactory = new ClientDescriptorFactory();
    private SerializerFactory serializerFactory = Environment.getSerializerFactory();
    private boolean outParametersMode = Environment.isOutParametersMode();
    private ValueUpdater updater = ValueUpdaterImpl.INSTANCE;
    private boolean invokeBaseObjectMethods = Environment.isInvokeBaseObjectMethods();
    private boolean debug = Environment.isDebug();
    private String baseUrl;
    private URLHandler<?> urlHandler;
    private String sessionId;

    public ClientFactoryImpl() {}

    public ClientFactoryImpl(String baseUrl) {
        setBaseUrl(baseUrl);
    }

    @Override
    public <T> T getClient(Class<T> serviceClass, String serviceName) {
        return getProxy(serviceClass, serviceName, true);
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

    public boolean isInvokeBaseObjectMethods() {
        return invokeBaseObjectMethods;
    }

    public void setInvokeBaseObjectMethods(boolean invokeBaseObjectMethods) {
        this.invokeBaseObjectMethods = invokeBaseObjectMethods;
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

    public URLHandler<?> getUrlHandler() {
        if (urlHandler == null) {
            try {
                urlHandler = Environment.getURLHandler(new URI(baseUrl).getScheme());
            } catch (URISyntaxException e) {
                LOG.warn("Can't initialize default client URL handler for URL " + baseUrl, e);
            }
        }
        return urlHandler;
    }

    public void setUrlHandler(URLHandler<?> urlHandler) {
        this.urlHandler = urlHandler;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    protected ServiceMethodInterceptor getMethodInterceptor(ServiceLocator serviceLocator, boolean useDescriptors) {
        return new ServiceMethodInterceptor(this, serviceLocator, useDescriptors);
    }

    @SuppressWarnings("unchecked")
    private <T> T getProxy(Class<T> serviceClass, String serviceName, boolean useDescriptors) {
        return (T) Enhancer.create(serviceClass, getMethodInterceptor(ServiceLocator.qualified(baseUrl, serviceName), useDescriptors));
    }

    private class ClientDescriptorFactory extends CachingDescriptorFactory {

        private DescriptorFactory remoteDescriptorFactory = null;

        @Override
        protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
            return getRemoteFactory().getClassDescriptor(cls);
        }

        private synchronized DescriptorFactory getRemoteFactory() {
            if (remoteDescriptorFactory == null)
                remoteDescriptorFactory = getProxy(DescriptorFactory.class, DescriptorFactory.NAME, false);
            return remoteDescriptorFactory;
        }

    }

}
