package org.zenframework.easyservices.descriptor;

public interface ServiceDescriptorFactory {

    ServiceDescriptor getServiceDescriptor(Class<?> serviceClass);

}
