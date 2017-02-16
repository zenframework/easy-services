package org.zenframework.easyservices.descriptor;

public interface DescriptorExtractor {

    ClassDescriptor getClassDescriptor(Class<?> cls);

    MethodDescriptor getMethodDescriptor(MethodIdentifier methodId);

}
