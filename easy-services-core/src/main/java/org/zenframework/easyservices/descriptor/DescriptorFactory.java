package org.zenframework.easyservices.descriptor;

public interface DescriptorFactory {

    String NAME = "descriptorFactory";

    ClassDescriptor getClassDescriptor(Class<?> cls);

}
