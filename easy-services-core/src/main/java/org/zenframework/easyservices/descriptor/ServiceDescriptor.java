package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ServiceDescriptor {

    private final Map<MethodIdentifier, MethodDescriptor> methodDescriptors = new HashMap<MethodIdentifier, MethodDescriptor>();

    public Map<MethodIdentifier, MethodDescriptor> getMethodDescriptors() {
        return methodDescriptors;
    }

    public void setMethodDescriptors(Map<MethodIdentifier, MethodDescriptor> methodDescriptors) {
        this.methodDescriptors.clear();
        this.methodDescriptors.putAll(methodDescriptors);
    }

    public MethodDescriptor getMethodDescriptor(Method method) {
        return methodDescriptors.get(new MethodIdentifier(method.getName(), method.getParameterTypes()));
    }

    public void setMethodDescriptor(Method method, MethodDescriptor methodDescriptor) {
        this.methodDescriptors.put(new MethodIdentifier(method), methodDescriptor);
    }

    public static MethodDescriptor getMethodDescriptor(ServiceDescriptor serviceDescriptor, Method method) {
        return serviceDescriptor != null ? serviceDescriptor.getMethodDescriptor(method) : null;
    }

    public static ValueDescriptor getReturnDescriptor(ServiceDescriptor serviceDescriptor, Method method) {
        MethodDescriptor methodDescriptor = getMethodDescriptor(serviceDescriptor, method);
        return methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
    }

    public static ValueDescriptor[] getArgumentDescriptors(ServiceDescriptor serviceDescriptor, Method method) {
        MethodDescriptor methodDescriptor = getMethodDescriptor(serviceDescriptor, method);
        return methodDescriptor != null ? methodDescriptor.getArgumentDescriptors() : new ValueDescriptor[method.getParameterTypes().length];
    }

}
