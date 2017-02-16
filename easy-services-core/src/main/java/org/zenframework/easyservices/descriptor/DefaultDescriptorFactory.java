package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zenframework.easyservices.Environment;

public class DefaultDescriptorFactory extends CachingDescriptorFactory {

    private final List<DescriptorExtractor> extractors = new ArrayList<DescriptorExtractor>(getDefaultExtractors());

    private boolean autoAliasing = Environment.isAutoAliasing();

    public void setAutoAliasing(boolean autoAliasing) {
        this.autoAliasing = autoAliasing;
    }

    public void setExtractors(List<DescriptorExtractor> extractors) {
        this.extractors.clear();
        this.extractors.addAll(getDefaultExtractors());
        this.extractors.addAll(extractors);
    }

    public List<DescriptorExtractor> getExtractors() {
        return extractors;
    }

    protected List<DescriptorExtractor> getDefaultExtractors() {
        return Arrays.<DescriptorExtractor> asList(AnnotationDescriptorExtractor.INSTANCE);
    }

    protected String getDefaultAlias(MethodIdentifier methodId) {
        StringBuilder str = new StringBuilder();
        str.append(methodId.getName());
        for (Class<?> paramType : methodId.getParameterTypes()) {
            char[] paramName = paramType.getSimpleName().replace("[]", "Array").toCharArray();
            paramName[0] = Character.toUpperCase(paramName[0]);
            str.append(paramName);
        }
        return str.toString();
    }

    @Override
    protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
        List<Class<?>> classes = DescriptorUtil.getAllAssignableFrom(cls);
        ClassDescriptor classDescriptor = new ClassDescriptor();
        for (Class<?> c : classes)
            for (DescriptorExtractor extractor : extractors)
                classDescriptor = update(classDescriptor, extractor.getClassDescriptor(c));
        return classDescriptor;
    }

    @Override
    protected MethodDescriptor extractMethodDescriptor(MethodIdentifier methodId) {
        List<Class<?>> classes = DescriptorUtil.getAllAssignableFrom(methodId.getMethodClass());
        MethodDescriptor methodDescriptor = getDefaultMethodDescriptor(methodId);
        for (Class<?> c : classes)
            for (DescriptorExtractor extractor : extractors)
                methodDescriptor = update(methodDescriptor, extractor
                        .getMethodDescriptor(new MethodIdentifier(c, methodId.getName(), methodId.getParameterTypes(), methodId.getReturnType())));
        if (autoAliasing) {
            int count = 0;
            for (Method method : methodId.getMethodClass().getMethods())
                if (method.getName().equals(methodId.getName()))
                    count++;
            if (count > 1) {
                if (methodDescriptor == null)
                    methodDescriptor = new MethodDescriptor(methodId.getParameterTypes().length);
                methodDescriptor.setAlias(getDefaultAlias(methodId));
            }
        }
        return methodDescriptor;
    }

    private MethodDescriptor getDefaultMethodDescriptor(MethodIdentifier methodId) {
        MethodDescriptor methodDescriptor = new MethodDescriptor(methodId.getParameterTypes().length);
        methodDescriptor.setReturnDescriptor(getClassDescriptor(methodId.getReturnType()).getValueDescriptor());
        for (int i = 0; i < methodId.getParameterTypes().length; i++)
            methodDescriptor.setParameterDescriptor(i, getClassDescriptor(methodId.getParameterTypes()[i]).getValueDescriptor());
        methodDescriptor.setDebug(getClassDescriptor(methodId.getMethodClass()).getDebug());
        return methodDescriptor;
    }

    private static ClassDescriptor update(ClassDescriptor oldValue, ClassDescriptor newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            ValueDescriptor valueDescriptor = newValue.getValueDescriptor();
            if (valueDescriptor != null)
                oldValue.setValueDescriptor(valueDescriptor);
            Boolean debug = newValue.getDebug();
            if (debug != null)
                oldValue.setDebug(debug);
        }
        return oldValue;
    }

    private static MethodDescriptor update(MethodDescriptor oldValue, MethodDescriptor newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            String alias = newValue.getAlias();
            if (alias != null)
                oldValue.setAlias(alias);
            ValueDescriptor returnDescriptor = newValue.getReturnDescriptor();
            if (returnDescriptor != null)
                oldValue.setReturnDescriptor(returnDescriptor);
            for (int i = 0; i < newValue.getParameterDescriptors().length; i++) {
                ValueDescriptor paramDescriptor = newValue.getParameterDescriptors()[i];
                if (paramDescriptor != null)
                    oldValue.setParameterDescriptor(i, paramDescriptor);
            }
        }
        return oldValue;
    }

}
