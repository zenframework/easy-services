package org.zenframework.easyservices.descriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClassDescriptorFactory implements ClassDescriptorFactory {

    protected final Map<Class<?>, ClassDescriptor> cache = Collections.synchronizedMap(new HashMap<Class<?>, ClassDescriptor>());

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        ClassDescriptor classDescriptor;
        classDescriptor = cache.get(cls);
        if (classDescriptor == null) {
            classDescriptor = extractClassDescriptor(cls);
            cache.put(cls, classDescriptor);
        }
        return classDescriptor;
    }

    protected abstract ClassDescriptor extractClassDescriptor(Class<?> cls);

}
