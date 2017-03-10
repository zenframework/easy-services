package org.zenframework.easyservices.descriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.zenframework.easyservices.annotations.Alias;
import org.zenframework.easyservices.annotations.Close;
import org.zenframework.easyservices.annotations.Debug;
import org.zenframework.easyservices.annotations.Out;
import org.zenframework.easyservices.annotations.Ref;
import org.zenframework.easyservices.annotations.TypeParameters;

public class AnnotationDescriptorExtractor implements DescriptorExtractor {

    public static final AnnotationDescriptorExtractor INSTANCE = new AnnotationDescriptorExtractor();

    @Override
    public MethodDescriptor extractMethodDescriptor(Class<?> cls, MethodIdentifier methodId) {
        try {

            Method method = cls.getMethod(methodId.getName(), methodId.getParameterTypes());
            Alias alias = method.getAnnotation(Alias.class);
            Close close = method.getAnnotation(Close.class);
            Ref[] refParams = getParamAnnotations(method, Ref.class, new Ref[methodId.getParameterTypes().length]);
            Close[] closeParams = getParamAnnotations(method, Close.class, new Close[methodId.getParameterTypes().length]);
            Out[] outParams = getParamAnnotations(method, Out.class, new Out[methodId.getParameterTypes().length]);
            TypeParameters[] paramTypeParams = getParamAnnotations(method, TypeParameters.class,
                    new TypeParameters[methodId.getParameterTypes().length]);
            Ref returnRef = method.getAnnotation(Ref.class);
            TypeParameters returnTypeParams = method.getAnnotation(TypeParameters.class);
            Debug debug = method.getAnnotation(Debug.class);

            MethodDescriptor methodDescriptor = new MethodDescriptor(refParams.length);
            boolean useful = false;
            if (alias != null) {
                methodDescriptor.setAlias(alias.value());
                useful = true;
            }
            if (close != null) {
                methodDescriptor.setClose(true);
                useful = true;
            }
            if (returnRef != null || returnTypeParams != null) {
                methodDescriptor.setReturnDescriptor(getValueDescriptor(returnRef, returnTypeParams));
                useful = true;
            }
            if (debug != null) {
                methodDescriptor.setDebug(debug.value());
                useful = true;
            }
            for (int i = 0; i < refParams.length; i++) {
                if (refParams[i] != null || closeParams[i] != null || outParams[i] != null || paramTypeParams[i] != null) {
                    methodDescriptor.setParameterDescriptor(i, getParamDescriptor(refParams[i], outParams[i], closeParams[i], paramTypeParams[i]));
                    useful = true;
                }
            }

            return useful ? methodDescriptor : null;

        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public ClassDefaults extractClassDefaults(Class<?> cls) {
        ClassDefaults classDefaults = new ClassDefaults();
        Ref ref = cls.getAnnotation(Ref.class);
        TypeParameters typeParams = cls.getAnnotation(TypeParameters.class);
        Debug clsDebug = cls.getAnnotation(Debug.class);
        if (ref != null || typeParams != null)
            classDefaults.setValueDescriptor(getValueDescriptor(ref, typeParams));
        if (clsDebug != null)
            classDefaults.setDebug(clsDebug.value());
        return classDefaults;
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

    private static ValueDescriptor getValueDescriptor(Ref ref, TypeParameters typeParams) {
        ValueDescriptor descriptor = new ValueDescriptor();
        if (ref != null)
            descriptor.setTransfer(ValueTransfer.REF);
        if (typeParams != null)
            descriptor.setTypeParameters(typeParams.value());
        return descriptor;
    }

    private static ParamDescriptor getParamDescriptor(Ref ref, Out out, Close close, TypeParameters typeParams) {
        if (ref == null && out == null && close == null && typeParams == null)
            return null;
        ParamDescriptor descriptor = new ParamDescriptor();
        if (out != null)
            descriptor.setTransfer(ValueTransfer.OUT);
        if (ref != null)
            descriptor.setTransfer(ValueTransfer.REF);
        if (close != null)
            descriptor.setClose(true);
        if (typeParams != null)
            descriptor.setTypeParameters(typeParams.value());
        return descriptor;
    }

}
