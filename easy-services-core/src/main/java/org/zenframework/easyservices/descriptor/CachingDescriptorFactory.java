package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class CachingDescriptorFactory implements DescriptorFactory {

    private final Map<Class<?>, ClassHolder> classes = new HashMap<Class<?>, ClassHolder>();

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        ClassHolder classHolder = getClassHolder(cls);
        synchronized (classHolder) {
            if (classHolder.classDescriptor == null)
                classHolder.classDescriptor = extractClassDescriptor(cls);
            return classHolder.classDescriptor;
        }
    }

    @Override
    public MethodDescriptor getMethodDescriptor(MethodIdentifier methodId) {
        return getMethodDescriptors(methodId.getMethodClass()).get(methodId);
    }

    @Override
    public Map<MethodIdentifier, MethodDescriptor> getMethodDescriptors(Class<?> cls) {
        ClassHolder classHolder = getClassHolder(cls);
        synchronized (classHolder) {
            if (classHolder.methodsById == null)
                classHolder.methodsById = extractMethodDescriptors(cls);
            return classHolder.methodsById;
        }
    }

    @Override
    public Map.Entry<MethodIdentifier, MethodDescriptor> getMethodEntry(Class<?> cls, String alias) {
        ClassHolder classHolder = getClassHolder(cls);
        synchronized (classHolder) {
            if (classHolder.methodsById == null)
                classHolder.methodsById = extractMethodDescriptors(cls);
            if (classHolder.methodsByAlias == null) {
                classHolder.methodsByAlias = new HashMap<String, Map.Entry<MethodIdentifier, MethodDescriptor>>();
                for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : classHolder.methodsById.entrySet())
                    if (entry.getValue().getAlias() != null)
                        classHolder.methodsByAlias.put(entry.getValue().getAlias(), entry);
            }
            return classHolder.methodsByAlias.get(alias);
        }
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

    private ClassHolder getClassHolder(Class<?> cls) {
        synchronized (classes) {
            ClassHolder classHolder = classes.get(cls);
            if (classHolder == null) {
                classHolder = new ClassHolder();
                classes.put(cls, classHolder);
            }
            return classHolder;
        }
    }

    private static class ClassHolder {

        ClassDescriptor classDescriptor;
        Map<MethodIdentifier, MethodDescriptor> methodsById;
        Map<String, Map.Entry<MethodIdentifier, MethodDescriptor>> methodsByAlias;

    }

}
