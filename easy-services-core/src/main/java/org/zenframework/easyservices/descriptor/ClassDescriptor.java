package org.zenframework.easyservices.descriptor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ClassDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<MethodIdentifier, MethodDescriptor> methodDescriptors = new HashMap<MethodIdentifier, MethodDescriptor>();

    public Map<MethodIdentifier, MethodDescriptor> getMethodDescriptors() {
        return methodDescriptors;
    }

    public void setMethodDescriptors(Map<MethodIdentifier, MethodDescriptor> methodDescriptors) {
        this.methodDescriptors.clear();
        this.methodDescriptors.putAll(methodDescriptors);
    }

    public MethodDescriptor getMethodDescriptor(MethodIdentifier methodId) {
        return methodDescriptors.get(methodId);
    }

    public void setMethodDescriptor(MethodIdentifier methodId, MethodDescriptor methodDescriptor) {
        methodDescriptors.put(methodId, methodDescriptor);
    }

}
