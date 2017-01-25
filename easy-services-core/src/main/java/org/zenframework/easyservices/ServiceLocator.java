package org.zenframework.easyservices;

import org.apache.commons.lang.ObjectUtils;

public class ServiceLocator {

    private final String baseUrl;
    private final String serviceName;
    private final String serviceUrl;

    private ServiceLocator(String baseUrl, String serviceName, String serviceUrl) {
        this.baseUrl = baseUrl;
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public boolean isQualified() {
        return baseUrl != null && serviceName != null && serviceUrl != null;
    }

    public boolean isAbsolute() {
        return serviceUrl != null;
    }

    public boolean isRelative() {
        return serviceName != null;
    }

    public static ServiceLocator relative(String servicePath) {
        return new ServiceLocator(null, servicePath, null);
    }

    public static ServiceLocator absolute(String serviceUrl) {
        return new ServiceLocator(null, null, serviceUrl);
    }

    public static ServiceLocator qualified(String baseUrl, String servicePath) {
        return new ServiceLocator(baseUrl, servicePath, baseUrl + servicePath);
    }

    @Override
    public int hashCode() {
        return isAbsolute() ? serviceUrl.hashCode() : serviceName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServiceLocator))
            return false;
        ServiceLocator sl = (ServiceLocator) obj;
        return isAbsolute() ? ObjectUtils.equals(serviceUrl, sl.getServiceUrl()) : ObjectUtils.equals(serviceName, sl.getServiceName());
    }

    @Override
    public String toString() {
        return isAbsolute() ? serviceUrl : serviceName;
    }

}
