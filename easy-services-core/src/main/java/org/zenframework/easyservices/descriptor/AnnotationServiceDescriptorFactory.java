package org.zenframework.easyservices.descriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.annotations.Alias;
import org.zenframework.easyservices.annotations.Value;

public class AnnotationServiceDescriptorFactory implements ServiceDescriptorFactory {

    private final Map<String, ServiceDescriptor> serviceDescriptorsCache = new HashMap<String, ServiceDescriptor>();

    @Override
    public ServiceDescriptor getServiceDescriptor(Class<?> serviceClass, String serviceName) {
        ServiceDescriptor descriptor;
        synchronized (serviceDescriptorsCache) {
            descriptor = serviceDescriptorsCache.get(serviceName);
        }
        if (descriptor == null) {
            descriptor = getServiceDescriptor(serviceClass);
            synchronized (serviceDescriptorsCache) {
                serviceDescriptorsCache.put(serviceName, descriptor);
            }
        }
        return descriptor;
    }

    private ServiceDescriptor getServiceDescriptor(Class<?> serviceClass) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor();
        for (Method method : serviceClass.getMethods()) {
            Alias methodAlias = findMethodAnnotation(method, Alias.class);
            Value returnValue = findMethodAnnotation(method, Value.class);
            try {
                MethodDescriptor methodDescriptor = new MethodDescriptor(method.getParameterTypes().length);
                boolean useful = false;
                if (methodAlias != null) {
                    methodDescriptor.setAlias(methodAlias.value());
                    useful = true;
                }
                if (returnValue != null) {
                    ValueDescriptor returnDescriptor = new ValueDescriptor();
                    returnDescriptor.setTypeParameters(returnValue.typeParameters());
                    int modifiers = returnValue.serializerAdapter().getModifiers();
                    if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers))
                        returnDescriptor.setSerializerAdapter(returnValue.serializerAdapter().newInstance());
                    returnDescriptor.setDynamicService(returnValue.dynamicService());
                    methodDescriptor.setReturnDescriptor(returnDescriptor);
                    useful = true;
                }
                Value[] argValues = findArgsAnnotations(method, Value.class, new Value[method.getParameterTypes().length]);
                for (int i = 0; i < argValues.length; i++) {
                    Value argValue = argValues[i];
                    if (argValue != null) {
                        ValueDescriptor argDescriptor = new ValueDescriptor();
                        argDescriptor.setTypeParameters(argValue.typeParameters());
                        int modifiers = argValue.serializerAdapter().getModifiers();
                        if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers))
                            argDescriptor.setSerializerAdapter(argValue.serializerAdapter().newInstance());
                        argDescriptor.setDynamicService(argValue.dynamicService());
                        methodDescriptor.setArgumentDescriptor(i, argDescriptor);
                        useful = true;
                    }
                }
                if (useful)
                    serviceDescriptor.setMethodDescriptor(method, methodDescriptor);
            } catch (Exception e) {
                throw new RuntimeException("Can't initialize method descriptor " + method, e);
            }
        }
        return serviceDescriptor;
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

}
