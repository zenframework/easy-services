package org.zenframework.easyservices.descriptor;

public interface DescriptorExtractor {

    ClassDefaults extractClassDefaults(Class<?> cls);

    MethodDescriptor extractMethodDescriptor(Class<?> cls, MethodIdentifier methodId);

}
