package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClassDescriptorFactory implements ClassDescriptorFactory {

    protected final Map<String, ClassDescriptor> cache = Collections.synchronizedMap(new HashMap<String, ClassDescriptor>());

    private boolean autoAliasing = false;

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        String className = cls.getName();
        ClassDescriptor classDescriptor;
        classDescriptor = cache.get(className);
        if (classDescriptor == null && !cache.containsKey(className)) {
            classDescriptor = extractClassDescriptor(cls);
            if (autoAliasing)
                classDescriptor = autoAlias(cls, classDescriptor);
            cache.put(className, classDescriptor);
        }
        return classDescriptor;
    }

    public void setAutoAliasing(boolean autoAliasing) {
        this.autoAliasing = autoAliasing;
    }

    protected abstract ClassDescriptor extractClassDescriptor(Class<?> cls);

    protected String getDefaultAlias(Method method) {
        StringBuilder str = new StringBuilder();
        str.append(method.getName());
        for (Class<?> paramType : method.getParameterTypes()) {
            char[] paramName = paramType.getSimpleName().replace("[]", "Array").toCharArray();
            paramName[0] = Character.toUpperCase(paramName[0]);
            str.append(paramName);
        }
        return str.toString();
    }

    private ClassDescriptor autoAlias(Class<?> cls, ClassDescriptor classDescriptor) {
        Method[] methods = cls.getMethods();
        if (methods.length < 2)
            return classDescriptor;
        Arrays.sort(methods, new Comparator<Method>() {

            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            boolean repeat = i < methods.length - 1 && method.getName().equals(methods[i + 1].getName())
                    || i > 0 && method.getName().equals(methods[i - 1].getName());
            if (repeat) {
                if (classDescriptor == null)
                    classDescriptor = new ClassDescriptor();
                MethodIdentifier methodId = new MethodIdentifier(method);
                MethodDescriptor methodDescriptor = classDescriptor.getMethodDescriptor(methodId);
                if (methodDescriptor == null) {
                    methodDescriptor = new MethodDescriptor(method.getParameterTypes().length);
                    classDescriptor.setMethodDescriptor(methodId, methodDescriptor);
                }
                if (methodDescriptor.getAlias() == null)
                    methodDescriptor.setAlias(getDefaultAlias(method));
            }
        }
        return classDescriptor;
    }

}
