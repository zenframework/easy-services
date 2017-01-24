package org.zenframework.easyservices.impl;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.descriptor.ServiceDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvocationHandler implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvocationHandler.class);

    private final String serviceUrl;
    private final ServiceDescriptor serviceDescriptor;
    private final SerializerFactory<?> serializerFactory;
    private final RequestMapper requestMapper;

    public ServiceInvocationHandler(String serviceUrl, ServiceDescriptor serviceDescriptor, SerializerFactory<?> serializerFactory,
            RequestMapper requestMapper) {
        this.serviceUrl = serviceUrl;
        this.serviceDescriptor = serviceDescriptor;
        this.serializerFactory = serializerFactory;
        this.requestMapper = requestMapper;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Serializer serializer = serializerFactory.getSerializer();
        String serializedArgs = serializer.compile(serializer.serialize(args, ServiceDescriptor.getArgumentDescriptors(serviceDescriptor, method)));
        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceUrl + ' ' + method.getName() + serializedArgs, LOG);
        try {
            URL url = requestMapper.getRequestURI(serviceUrl, method.getName(), serializedArgs).toURL();
            StringBuilder str = new StringBuilder(8192);
            char buf[] = new char[8192];
            InputStreamReader reader = new InputStreamReader(url.openStream());
            try {
                for (int n = reader.read(buf); n >= 0; n = reader.read(buf))
                    str.append(new String(buf, 0, n));
            } finally {
                reader.close();
            }
            if (time != null)
                time.printDifference(str);
            return serializer.deserialize(serializer.parse(str.toString()), method.getReturnType(),
                    ServiceDescriptor.getReturnDescriptor(serviceDescriptor, method));
        } catch (Throwable e) {
            if (time != null)
                time.printDifference(e);
            throw e;
        }
    }

}
