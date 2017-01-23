package org.zenframework.easyservices.descriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.zenframework.easyservices.annotations.Alias;
import org.zenframework.easyservices.annotations.Value;
import org.zenframework.easyservices.serialize.SerializerAdapter;

public class AnnotationServiceDescriptorFactory implements ServiceDescriptorFactory {

    @SuppressWarnings("rawtypes")
    @Override
    public ServiceDescriptor getServiceDescriptor(Class<?> serviceClass) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor();
        for (Method method : serviceClass.getMethods()) {
            Alias methodAlias = method.getAnnotation(Alias.class);
            Value returnValue = method.getAnnotation(Value.class);
            try {
                MethodDescriptor methodDescriptor = new MethodDescriptor();
                if (methodAlias != null)
                    methodDescriptor.setAlias(methodAlias.value());
                if (returnValue != null) {
                    ValueDescriptor returnDescriptor = new ValueDescriptor();
                    returnDescriptor.setTypeParameters(returnValue.typeParameters());
                    Class<? extends SerializerAdapter> serializerAdapterClass = returnValue.serializerAdapter();
                    int modifiers = serializerAdapterClass.getModifiers();
                    if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers))
                        returnDescriptor.setSerializerAdapter(returnValue.serializerAdapter().newInstance());
                    returnDescriptor.setDynamicService(returnValue.dynamicService());
                    methodDescriptor.setReturnDescriptor(returnDescriptor);
                }
                Annotation[][] annotations = method.getParameterAnnotations();
                for (int i = 0; i < annotations.length; i++) {
                    Value argValue = null;
                    for (Annotation annotation : annotations[i]) {
                        if (annotation instanceof Value) {
                            argValue = (Value) annotation;
                            break;
                        }
                    }
                    if (argValue != null) {
                        ValueDescriptor argDescriptor = new ValueDescriptor();
                        argDescriptor.setTypeParameters(argValue.typeParameters());
                        if (argValue.serializerAdapter() != null)
                            argDescriptor.setSerializerAdapter(argValue.serializerAdapter().newInstance());
                        argDescriptor.setDynamicService(argValue.dynamicService());
                        methodDescriptor.getArgumentDescriptors().put(i, argDescriptor);
                    }
                }
                if (methodAlias != null || returnValue != null || !methodDescriptor.getArgumentDescriptors().isEmpty())
                    serviceDescriptor.setMethodDescriptor(method, methodDescriptor);
            } catch (Exception e) {
                throw new RuntimeException("Can't initialize method descriptor " + method, e);
            }
        }
        return serviceDescriptor;
    }

}
