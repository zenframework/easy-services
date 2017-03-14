package org.zenframework.easyservices.util.bean;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);

    private ServiceUtil() {}

    public static <T> Set<T> getServices(Class<T> serviceClass) {
        ServiceLoader<T> serializaerLoader = ServiceLoader.load(serviceClass);
        Set<T> services = new HashSet<T>();
        Iterator<T> i = serializaerLoader.iterator();
        StringBuilder str = new StringBuilder("Services loaded:\n\t").append(serviceClass);
        while (i.hasNext()) {
            T service = i.next();
            services.add(service);
            str.append("\n\t\t- ").append(service);
        }
        if (services.isEmpty())
            str.append("\n\t\t- <no appropriate services>");
        LOG.debug(str.toString());
        return services;
    }

    public static <T> T getService(Class<T> serviceClass) {
        Set<T> services = getServices(serviceClass);
        return services.size() > 0 ? services.iterator().next() : null;
    }

}
