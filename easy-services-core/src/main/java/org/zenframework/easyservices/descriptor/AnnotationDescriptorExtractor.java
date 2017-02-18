package org.zenframework.easyservices.descriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.zenframework.easyservices.annotations.Alias;
import org.zenframework.easyservices.annotations.Debug;
import org.zenframework.easyservices.annotations.Value;

public class AnnotationDescriptorExtractor implements DescriptorExtractor {

    public static final AnnotationDescriptorExtractor INSTANCE = new AnnotationDescriptorExtractor();

    @Override
    public MethodDescriptor getMethodDescriptor(MethodIdentifier methodId) {
        try {
            Method method = methodId.getMethodClass().getMethod(methodId.getName(), methodId.getParameterTypes());
            Alias methodAlias = method.getAnnotation(Alias.class);
            Value[] paramValues = getParamAnnotations(method, Value.class, new Value[methodId.getParameterTypes().length]);
            Value returnValue = method.getAnnotation(Value.class);
            Debug methodDebug = method.getAnnotation(Debug.class);
            return getMethodDescriptor(methodAlias, paramValues, returnValue, methodDebug);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        Value value = cls.getAnnotation(Value.class);
        Debug clsDebug = cls.getAnnotation(Debug.class);
        if (value != null)
            classDescriptor.setValueDescriptor(getValueDescriptor(value));
        if (clsDebug != null)
            classDescriptor.setDebug(clsDebug.value());
        return classDescriptor;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] getParamAnnotations(Method method, Class<?> annotationClass, T[] annotations) {
        Annotation[][] candidates = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation candidate : candidates[i]) {
                if (annotationClass.isInstance(candidate)) {
                    annotations[i] = (T) candidate;
                    break;
                }
            }
        }
        return annotations;
    }

    private static ValueDescriptor getValueDescriptor(Value value) {
        if (value == null)
            return null;
        try {
            ValueDescriptor descriptor = new ValueDescriptor();
            descriptor.setTypeParameters(value.typeParameters());
            Class<?>[] adapterClasses = value.adapters();
            for (Class<?> cls : adapterClasses)
                descriptor.addAdapter(cls.newInstance());
            descriptor.setTransfer(value.transfer());
            return descriptor;
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't get value descriptor", e);
        }
    }

    private static MethodDescriptor getMethodDescriptor(Alias alias, Value[] paramValues, Value returnValue, Debug debug) {
        MethodDescriptor methodDescriptor = new MethodDescriptor(paramValues.length);
        boolean useful = false;
        if (alias != null) {
            methodDescriptor.setAlias(alias.value());
            useful = true;
        }
        if (returnValue != null) {
            methodDescriptor.setReturnDescriptor(getValueDescriptor(returnValue));
            useful = true;
        }
        if (debug != null) {
            methodDescriptor.setDebug(debug.value());
            useful = true;
        }
        for (int i = 0; i < paramValues.length; i++) {
            Value paramValue = paramValues[i];
            if (paramValue != null) {
                methodDescriptor.setParameterDescriptor(i, getValueDescriptor(paramValue));
                useful = true;
            }
        }
        return useful ? methodDescriptor : null;
    }

}
