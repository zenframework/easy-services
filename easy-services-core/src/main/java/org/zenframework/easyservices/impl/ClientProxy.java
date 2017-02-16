package org.zenframework.easyservices.impl;

import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.update.ValueUpdater;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;

@SuppressWarnings("unchecked")
public class ClientProxy {

    private ClientProxy() {}

    public static <T> T getCGLibProxy(Class<T> serviceClass, ServiceLocator serviceLocator, DescriptorFactory classDescriptorFactory,
            SerializerFactory serializerFactory, ValueUpdater updater, boolean debug) {
        return (T) Enhancer.create(serviceClass,
                new ServiceMethodInterceptor(serviceLocator, classDescriptorFactory, serializerFactory, updater, debug));
    }

    public static <T extends MethodInterceptor> T getMethodInterceptor(Object proxy, Class<T> interceptorClass) {
        if (proxy instanceof Factory) {
            Factory factory = (Factory) proxy;
            Callback callback = factory.getCallback(0);
            if (interceptorClass.isInstance(callback)) {
                return (T) callback;
            }
        }
        throw new RuntimeException(proxy + " is not a proxy object");
    }

}
