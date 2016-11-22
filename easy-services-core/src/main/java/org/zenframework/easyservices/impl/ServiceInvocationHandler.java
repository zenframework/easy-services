package org.zenframework.easyservices.impl;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerAdapter;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvocationHandler implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvocationHandler.class);

    private final String serviceUrl;
    private final SerializerFactory<?> serializerFactory;
    private final RequestMapper serviceMapper;

    public ServiceInvocationHandler(String serviceUrl, SerializerFactory<?> serializerFactory, RequestMapper serviceMapper) {
        this.serviceUrl = serviceUrl;
        this.serializerFactory = serializerFactory;
        this.serviceMapper = serviceMapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Serializer<?> serializer = serializerFactory.getSerializer();
        String serializedArgs = args == null || args.length == 0 ? null : new String(serializer.serialize(args));
        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceUrl + ' ' + method.getName() + serializedArgs, LOG);
        try {
            URL url = serviceMapper.getRequestURI(serviceUrl, method.getName(), serializedArgs).toURL();
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
            return deserialize(serializer, method, str.toString());
        } catch (Throwable e) {
            if (time != null)
                time.printDifference(e);
            throw e;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object deserialize(Serializer<?> serializer, Method method, String str) throws SerializationException {
        org.zenframework.easyservices.annotations.SerializerAdapter serializerAdapterAnnotation = method
                .getAnnotation(org.zenframework.easyservices.annotations.SerializerAdapter.class);
        org.zenframework.easyservices.annotations.TypeParameters typeParametersAnnotation = method
                .getAnnotation(org.zenframework.easyservices.annotations.TypeParameters.class);
        SerializerAdapter adapter = null;
        try {
            if (serializerAdapterAnnotation != null)
                adapter = serializerAdapterAnnotation.value().newInstance();
        } catch (Exception e) {
            throw new SerializationException("Can't instantiate adapter " + serializerAdapterAnnotation.value());
        }
        if (adapter == null)
            adapter = serializerFactory.getAdapter(method.getReturnType());
        if (adapter != null)
            return serializer.deserialize(str, adapter, typeParametersAnnotation != null ? typeParametersAnnotation.value() : new Class<?>[0]);
        return serializer.deserialize(str, method.getReturnType());
    }

}
