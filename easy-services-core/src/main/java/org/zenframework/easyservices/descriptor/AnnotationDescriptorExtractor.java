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
            Alias methodAlias = method.getAnnotation(Alias.class); //findMethodAnnotation(method, Alias.class);
            Value[] paramValues = getParamAnnotations(method, Value.class, new Value[methodId.getParameterTypes().length]); //findArgsAnnotations(method, Value.class, new Value[paramTypes.length]);
            Value returnValue = method.getAnnotation(Value.class); //findMethodAnnotation(method, Value.class);
            Debug methodDebug = method.getAnnotation(Debug.class); //findMethodAnnotation(method, Debug.class);
            //for (int i = 0; i < paramValues.length; i++)
            //    if (paramValues[i] == null)
            //        paramValues[i] = paramTypes[i].getAnnotation(Value.class);
            //if (returnValue == null)
            //    returnValue = method.getReturnType().getAnnotation(Value.class);
            return getMethodDescriptor(methodAlias, paramValues, returnValue, methodDebug);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /*@Override
    public ValueDescriptor getDefaultValueDescriptor(Class<?> cls) {
        return getValueDescriptor(cls.getAnnotation(Value.class));
    }*/

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        Value value = cls.getAnnotation(Value.class);
        Debug clsDebug = cls.getAnnotation(Debug.class);
        if (value != null)
            classDescriptor.setValueDescriptor(getValueDescriptor(value));
        if (clsDebug != null)
            classDescriptor.setDebug(clsDebug.value());
        /*for (Method method : cls.getMethods()) {
            Class<?>[] paramTypes = method.getParameterTypes();
            Alias methodAlias = method.getAnnotation(Alias.class); //findMethodAnnotation(method, Alias.class);
            Value[] paramValues = getParamAnnotations(method, Value.class, new Value[paramTypes.length]); //findArgsAnnotations(method, Value.class, new Value[paramTypes.length]);
            Value returnValue = method.getAnnotation(Value.class); //findMethodAnnotation(method, Value.class);
            Debug methodDebug = method.getAnnotation(Debug.class); //findMethodAnnotation(method, Debug.class);
            //for (int i = 0; i < paramValues.length; i++)
            //    if (paramValues[i] == null)
            //        paramValues[i] = paramTypes[i].getAnnotation(Value.class);
            //if (returnValue == null)
            //    returnValue = method.getReturnType().getAnnotation(Value.class);
            MethodDescriptor methodDescriptor = getMethodDescriptor(methodAlias, paramValues, returnValue, methodDebug);
            if (methodDescriptor != null)
                classDescriptor.setMethodDescriptor(new MethodIdentifier(method), methodDescriptor);
        }*/
        return classDescriptor;
    }

    /*private static <T extends Annotation> T findMethodAnnotation(Method method, Class<T> annotationClass) {
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
    }*/

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
