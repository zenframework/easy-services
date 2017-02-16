package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class CachingDescriptorFactory implements DescriptorFactory {

    private final Map<Class<?>, ClassHolder> classes = Collections.synchronizedMap(new HashMap<Class<?>, ClassHolder>());

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        ClassHolder classHolder = classes.get(cls);
        if (classHolder == null) {
            classHolder = new ClassHolder();
            classes.put(cls, classHolder);
        }
        if (classHolder.classDescriptor == null)
            classHolder.classDescriptor = extractClassDescriptor(cls);
        return classHolder.classDescriptor;
    }

    @Override
    public MethodDescriptor getMethodDescriptor(MethodIdentifier methodId) {
        return getMethodDescriptors(methodId.getMethodClass()).get(methodId);
    }

    @Override
    public Map<MethodIdentifier, MethodDescriptor> getMethodDescriptors(Class<?> cls) {
        ClassHolder classHolder = classes.get(cls);
        if (classHolder == null) {
            classHolder = new ClassHolder();
            classes.put(cls, classHolder);
        }
        if (classHolder.methodDescriptors == null)
            classHolder.methodDescriptors = extractMethodDescriptors(cls);
        return classHolder.methodDescriptors;
    }

    protected Map<MethodIdentifier, MethodDescriptor> extractMethodDescriptors(Class<?> cls) {
        Map<MethodIdentifier, MethodDescriptor> methodDescriptors = new HashMap<MethodIdentifier, MethodDescriptor>();
        for (Method method : cls.getMethods()) {
            MethodIdentifier methodId = new MethodIdentifier(method);
            MethodDescriptor methodDescriptor = extractMethodDescriptor(methodId);
            if (methodDescriptor != null)
                methodDescriptors.put(methodId, methodDescriptor);
        }
        return methodDescriptors;
    }

    abstract protected ClassDescriptor extractClassDescriptor(Class<?> cls);

    abstract protected MethodDescriptor extractMethodDescriptor(MethodIdentifier methodId);

    private static class ClassHolder {

        ClassDescriptor classDescriptor;
        Map<MethodIdentifier, MethodDescriptor> methodDescriptors;

    }

}
