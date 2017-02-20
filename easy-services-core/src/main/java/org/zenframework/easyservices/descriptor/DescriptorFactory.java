package org.zenframework.easyservices.descriptor;

import java.util.Map;

public interface DescriptorFactory extends DescriptorExtractor {

    String NAME = "/descriptorFactory";

    Map<MethodIdentifier, MethodDescriptor> getMethodDescriptors(Class<?> cls);

    Map.Entry<MethodIdentifier, MethodDescriptor> getMethodEntry(Class<?> cls, String alias);

}
