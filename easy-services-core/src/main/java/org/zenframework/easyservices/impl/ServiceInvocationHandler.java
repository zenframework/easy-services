package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.ServiceDescriptor;
import org.zenframework.easyservices.descriptor.ServiceDescriptorFactory;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvocationHandler implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvocationHandler.class);

    private final ServiceLocator serviceLocator;
    private final Class<?> serviceClass;
    private final ServiceDescriptorFactory serviceDescriptorFactory;
    private final SerializerFactory<?> serializerFactory;
    private final RequestMapper requestMapper;

    public ServiceInvocationHandler(ServiceLocator serviceLocator, Class<?> serviceClass, ServiceDescriptorFactory serviceDescriptorFactory,
            SerializerFactory<?> serializerFactory, RequestMapper requestMapper) {
        this.serviceLocator = serviceLocator;
        this.serviceClass = serviceClass;
        this.serviceDescriptorFactory = serviceDescriptorFactory;
        this.serializerFactory = serializerFactory;
        this.requestMapper = requestMapper;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (serviceLocator.isRelative())
            throw new ClientException("Service locator '" + serviceLocator + "' must be absolute");
        Serializer serializer = serializerFactory.getSerializer();
        ServiceDescriptor serviceDescriptor = serviceDescriptorFactory.getServiceDescriptor(serviceClass);
        String serializedArgs = serializer.compile(serialize(serializer, args, ServiceDescriptor.getArgumentDescriptors(serviceDescriptor, method)));
        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceLocator.getServiceUrl() + ' ' + method.getName() + serializedArgs, LOG);
        try {
            URL url = requestMapper.getRequestURI(serviceLocator.getServiceUrl(), method.getName(), serializedArgs).toURL();
            String data = readData(url.openStream());
            if (time != null)
                time.printDifference(data);
            return deserialize(serializer, serializer.parse(data), method.getReturnType(),
                    ServiceDescriptor.getReturnDescriptor(serviceDescriptor, method));
        } catch (Throwable e) {
            if (time != null)
                time.printDifference(e);
            throw e;
        }
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    private static String readData(InputStream in) throws IOException {
        StringBuilder str = new StringBuilder(8192);
        char buf[] = new char[8192];
        InputStreamReader reader = new InputStreamReader(in);
        try {
            for (int n = reader.read(buf); n >= 0; n = reader.read(buf))
                str.append(new String(buf, 0, n));
            return str.toString();
        } finally {
            reader.close();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object deserialize(Serializer serializer, Object structure, Class<?> objType, ValueDescriptor valueDescriptor)
            throws SerializationException {
        if (valueDescriptor != null && valueDescriptor.isReference()) {
            ServiceLocator locator = (ServiceLocator) serializer.deserialize(structure, ServiceLocator.class);
            if (locator.isRelative())
                locator = ServiceLocator.qualified(serviceLocator.getBaseUrl(), locator.getServiceName());
            return getProxy(locator, objType);
        }
        return serializer.deserialize(structure, objType, valueDescriptor);

    }

    @SuppressWarnings({ "rawtypes" })
    private static Object[] serialize(Serializer serializer, Object[] array, ValueDescriptor[] valueDescriptors) {
        if (array == null)
            return null;
        Object[] structure = serializer.newArray(array.length);
        for (int i = 0; i < array.length; i++) {
            ValueDescriptor valueDescriptor = valueDescriptors[i];
            if (valueDescriptor != null && valueDescriptor.isReference()) {
                ServiceInvocationHandler handler = (ServiceInvocationHandler) Proxy.getInvocationHandler(array[i]);
                structure[i] = serializer.serialize(handler.getServiceLocator(), valueDescriptor);
            } else {
                structure[i] = serializer.serialize(array[i], valueDescriptor);
            }
        }
        return structure;
    }

    private Object getProxy(ServiceLocator serviceLocator, Class<?> serviceClass) throws IllegalArgumentException, ClientException {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { serviceClass },
                new ServiceInvocationHandler(serviceLocator, serviceClass, serviceDescriptorFactory, serializerFactory, requestMapper));
    }

}
