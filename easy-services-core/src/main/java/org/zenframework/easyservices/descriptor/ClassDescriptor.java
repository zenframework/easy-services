package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClassDescriptor {

    private final Map<MethodIdentifier, MethodDescriptor> methodDescriptors = new HashMap<MethodIdentifier, MethodDescriptor>();
    private ValueDescriptor valueDescriptor;

    public Map<MethodIdentifier, MethodDescriptor> getMethodDescriptors() {
        return methodDescriptors;
    }

    public void setMethodDescriptors(Map<MethodIdentifier, MethodDescriptor> methodDescriptors) {
        this.methodDescriptors.clear();
        this.methodDescriptors.putAll(methodDescriptors);
    }

    public MethodDescriptor getMethodDescriptor(Method method) {
        MethodDescriptor methodDescriptor = methodDescriptors.get(new MethodIdentifier(method));
        return methodDescriptor != null ? methodDescriptor : new MethodDescriptor(method.getParameterTypes().length);
    }

    public void setMethodDescriptor(Method method, MethodDescriptor methodDescriptor) {
        this.methodDescriptors.put(new MethodIdentifier(method), methodDescriptor);
    }

    public ValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

    public void setValueDescriptor(ValueDescriptor valueDescriptor) {
        this.valueDescriptor = valueDescriptor;
    }

}
