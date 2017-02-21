package org.zenframework.easyservices.descriptor;

import java.util.HashMap;
import java.util.Map;

public abstract class CachingDescriptorFactory implements DescriptorFactory {

    private final Map<Class<?>, ClassDescriptor> classes = new HashMap<Class<?>, ClassDescriptor>();

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        synchronized (classes) {
            ClassDescriptor classDescriptor = classes.get(cls);
            if (classDescriptor == null) {
                classDescriptor = extractClassDescriptor(cls);
                classes.put(cls, classDescriptor);
            }
            return classDescriptor;
        }
    }

    abstract protected ClassDescriptor extractClassDescriptor(Class<?> cls);

}
