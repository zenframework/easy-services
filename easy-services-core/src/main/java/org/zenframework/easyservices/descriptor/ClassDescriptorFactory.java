package org.zenframework.easyservices.descriptor;

public interface ClassDescriptorFactory {

    String NAME = "/classDescriptorFactory";

    ClassDescriptor getClassDescriptor(Class<?> cls);

}
