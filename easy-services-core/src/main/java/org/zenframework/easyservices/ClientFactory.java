package org.zenframework.easyservices;

public interface ClientFactory {

    <T> T getClient(Class<T> serviceClass, String serviceName);

}
