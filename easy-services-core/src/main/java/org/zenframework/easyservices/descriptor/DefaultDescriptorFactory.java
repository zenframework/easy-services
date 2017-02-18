package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zenframework.easyservices.Environment;

public class DefaultDescriptorFactory extends CachingDescriptorFactory {

    private static final String XML_DESCRIPTOR_PATH = "META-INF/easy-services/descriptor.xml";

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

    public void addXmlDescriptorExtractor(String url) {
        extractors.add(new XmlDescriptorExtractor(url));
    }

    protected List<DescriptorExtractor> getDefaultExtractors() {
        return Arrays.<DescriptorExtractor> asList(AnnotationDescriptorExtractor.INSTANCE, new ClasspathXmlDescriptorExtractor(XML_DESCRIPTOR_PATH));
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
                classDescriptor = DescriptorUtil.merge(classDescriptor, extractor.getClassDescriptor(c));
        return classDescriptor;
    }

    @Override
    protected MethodDescriptor extractMethodDescriptor(MethodIdentifier methodId) {
        List<Class<?>> classes = DescriptorUtil.getAllAssignableFrom(methodId.getMethodClass());
        MethodDescriptor methodDescriptor = getDefaultMethodDescriptor(methodId);
        for (Class<?> c : classes)
            for (DescriptorExtractor extractor : extractors)
                methodDescriptor = DescriptorUtil.merge(methodDescriptor, extractor
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

}
