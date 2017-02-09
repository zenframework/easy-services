package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.ClassDescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ServiceMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceMethodInterceptor.class);

    private final ServiceLocator serviceLocator;
    private final Class<?> serviceClass;
    private final ClassDescriptorFactory serviceDescriptorFactory;
    private final SerializerFactory serializerFactory;
    private final RequestMapper requestMapper;

    public ServiceMethodInterceptor(ServiceLocator serviceLocator, Class<?> serviceClass, ClassDescriptorFactory serviceDescriptorFactory,
            SerializerFactory serializerFactory, RequestMapper requestMapper) {
        this.serviceLocator = serviceLocator;
        this.serviceClass = serviceClass;
        this.serviceDescriptorFactory = serviceDescriptorFactory;
        this.serializerFactory = serializerFactory;
        this.requestMapper = requestMapper;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        if (args == null)
            args = new Object[0];

        // Check service locator is absolute
        if (serviceLocator.isRelative())
            throw new ClientException("Service locator '" + serviceLocator + "' must be absolute");

        Serializer serializer = serializerFactory.getSerializer();
        ClassDescriptor classDescriptor = serviceDescriptorFactory != null ? serviceDescriptorFactory.getClassDescriptor(serviceClass) : null;
        MethodDescriptor methodDescriptor = classDescriptor != null ? classDescriptor.getMethodDescriptor(method) : null;
        ValueDescriptor[] argDescriptors = methodDescriptor != null ? methodDescriptor.getParameterDescriptors() : new ValueDescriptor[args.length];

        // Find and replace proxy objects with references
        for (int i = 0; i < args.length; i++) {
            ValueDescriptor argDescriptor = argDescriptors[i];
            if (argDescriptor != null && argDescriptor.isReference()) {
                //ServiceInvocationHandler handler = (ServiceInvocationHandler) Proxy.getInvocationHandler(args[i]);
                ServiceMethodInterceptor intercpetor = ClientProxy.getMethodInterceptor(args[i], ServiceMethodInterceptor.class);
                args[i] = intercpetor.getServiceLocator();
            }
        }

        // Serialize arguments
        String serializedArgs = serializer.serialize(args);

        TimeChecker time = null;
        boolean debug = methodDescriptor != null ? methodDescriptor.isDebug() : classDescriptor != null ? classDescriptor.isDebug() : false;
        if (debug && LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceLocator.getServiceUrl() + ' ' + method.getName() + serializedArgs, LOG);

        InputStream in = null;
        try {

            // Call service
            URL url = requestMapper.getRequestURI(serviceLocator.getServiceUrl(), method.getName(), serializedArgs).toURL();
            URLConnection connection = url.openConnection();
            checkError(connection, serializer);
            in = connection.getInputStream();
            Object result = null;

            ValueDescriptor returnDescriptor = methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
            if (returnDescriptor != null && returnDescriptor.isReference()) {
                // If result is reference, replace with proxy object
                ServiceLocator locator = (ServiceLocator) serializer.deserialize(in, ServiceLocator.class, returnDescriptor);
                if (locator.isRelative())
                    locator = ServiceLocator.qualified(serviceLocator.getBaseUrl(), locator.getServiceName());
                result = ClientProxy.getCGLibProxy(method.getReturnType(), locator, serviceDescriptorFactory, serializerFactory, requestMapper);
            } else {
                // Else return deserialized result
                if (method.getReturnType() != void.class)
                    result = serializer.deserialize(in, method.getReturnType(), returnDescriptor);
            }

            if (time != null)
                time.printDifference(result);
            return result;

        } catch (Throwable e) {
            if (time != null)
                time.printDifference(e);
            throw e;
        } finally {
            if (in != null)
                in.close();
        }

    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    // TODO must be extendable
    private void checkError(URLConnection connection, Serializer serializer) throws SerializationException, IOException, Throwable {
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String data = httpConnection.getResponseMessage();
                throw (Throwable) serializer.deserialize(data, Throwable.class, null);
            }
        }
    }

}
