package org.zenframework.easyservices.descriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.annotations.Alias;
import org.zenframework.easyservices.annotations.Value;

public class AnnotationClassDescriptorFactory implements ClassDescriptorFactory {

    public static final AnnotationClassDescriptorFactory INSTANSE = new AnnotationClassDescriptorFactory();

    private final Map<Class<?>, ClassDescriptor> cache = new HashMap<Class<?>, ClassDescriptor>();

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        synchronized (cache) {
            ClassDescriptor classDescriptor = cache.get(cls);
            if (classDescriptor == null) {
                classDescriptor = extractClassDescriptor(cls);
                cache.put(cls, classDescriptor);
            }
            return classDescriptor;
        }
    }

    private static ClassDescriptor extractClassDescriptor(Class<?> cls) {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        Value value = cls.getAnnotation(Value.class);
        if (value != null)
            classDescriptor.setValueDescriptor(getValueDescriptor(value));
        for (Method method : cls.getMethods()) {
            Alias methodAlias = findMethodAnnotation(method, Alias.class);
            Value returnValue = findMethodAnnotation(method, Value.class);
            if (returnValue == null)
                returnValue = method.getReturnType().getAnnotation(Value.class);
            Class<?>[] argTypes = method.getParameterTypes();
            MethodDescriptor methodDescriptor = new MethodDescriptor(argTypes.length);
            boolean useful = false;
            if (methodAlias != null) {
                methodDescriptor.setAlias(methodAlias.value());
                useful = true;
            }
            if (returnValue != null) {
                methodDescriptor.setReturnDescriptor(getValueDescriptor(returnValue));
                useful = true;
            }
            Value[] argValues = findArgsAnnotations(method, Value.class, new Value[argTypes.length]);
            for (int i = 0; i < argValues.length; i++) {
                Value argValue = argValues[i];
                if (argValue == null)
                    argValue = argTypes[i].getAnnotation(Value.class);
                if (argValue != null) {
                    methodDescriptor.setParameterDescriptor(i, getValueDescriptor(argValue));
                    useful = true;
                }
            }
            if (useful)
                classDescriptor.setMethodDescriptor(method, methodDescriptor);
        }
        return classDescriptor;
    }

    private static <T extends Annotation> T findMethodAnnotation(Method method, Class<T> annotationClass) {
        List<Class<?>> classes = new ArrayList<Class<?>>(Arrays.asList(method.getDeclaringClass().getInterfaces()));
        classes.add(method.getDeclaringClass());
        T annotation = null;
        for (Class<?> cls : classes) {
            try {
                Method m = cls.getMethod(method.getName(), method.getParameterTypes());
                T candidate = m.getAnnotation(annotationClass);
                if (candidate != null)
                    annotation = candidate;
            } catch (NoSuchMethodException e) {}
        }
        return annotation;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T[] findArgsAnnotations(Method method, Class<T> annotationClass, T[] annotations) {
        List<Class<?>> classes = new ArrayList<Class<?>>(Arrays.asList(method.getDeclaringClass().getInterfaces()));
        classes.add(method.getDeclaringClass());
        for (Class<?> cls : classes) {
            try {
                Method m = cls.getMethod(method.getName(), method.getParameterTypes());
                Annotation[][] candidates = m.getParameterAnnotations();
                for (int i = 0; i < annotations.length; i++) {
                    for (Annotation candidate : candidates[i]) {
                        if (annotationClass.isInstance(candidate)) {
                            annotations[i] = (T) candidate;
                            break;
                        }
                    }
                }
            } catch (NoSuchMethodException e) {}
        }
        return annotations;
    }

    private static ValueDescriptor getValueDescriptor(Value value) {
        try {
            ValueDescriptor descriptor = new ValueDescriptor();
            descriptor.setTypeParameters(value.typeParameters());
            Class<?>[] adapterClasses = value.adapters();
            for (Class<?> cls : adapterClasses)
                descriptor.addAdapter(cls.newInstance());
            descriptor.setReference(value.reference());
            return descriptor;
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't get value descriptor", e);
        }
    }

}
