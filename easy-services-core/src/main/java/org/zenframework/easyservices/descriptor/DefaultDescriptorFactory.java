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
        ClassDescriptor classDescriptor = new ClassDescriptor();
        for (Method method : cls.getMethods()) {
            MethodIdentifier methodId = new MethodIdentifier(method);
            MethodDescriptor methodDescriptor = extractMethodDescriptor(cls, methodId);
            if (methodDescriptor != null)
                classDescriptor.setMethodDescriptor(methodId, methodDescriptor);
        }
        return classDescriptor;
    }

    private MethodDescriptor extractMethodDescriptor(Class<?> cls, MethodIdentifier methodId) {
        List<Class<?>> classes = DescriptorUtil.getAllAssignableFrom(cls);
        MethodDescriptor methodDescriptor = getDefaultMethodDescriptor(cls, methodId);
        for (Class<?> c : classes)
            for (DescriptorExtractor extractor : extractors)
                methodDescriptor = DescriptorUtil.merge(methodDescriptor, extractor.extractMethodDescriptor(c,
                        new MethodIdentifier(methodId.getName(), methodId.getParameterTypes(), methodId.getReturnType())));
        if (autoAliasing) {
            int count = 0;
            for (Method method : cls.getMethods())
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

    private MethodDescriptor getDefaultMethodDescriptor(Class<?> cls, MethodIdentifier methodId) {
        MethodDescriptor methodDescriptor = new MethodDescriptor(methodId.getParameterTypes().length);
        methodDescriptor.setReturnDescriptor(extractClassDefaults(methodId.getReturnType()).getValueDescriptor());
        for (int i = 0; i < methodId.getParameterTypes().length; i++) {
            ValueDescriptor defaultDescriptor = extractClassDefaults(methodId.getParameterTypes()[i]).getValueDescriptor();
            if (defaultDescriptor != null)
                methodDescriptor.setParameterDescriptor(i, new ParamDescriptor(defaultDescriptor));
        }
        methodDescriptor.setDebug(extractClassDefaults(cls).getDebug());
        return methodDescriptor;
    }

    private ClassDefaults extractClassDefaults(Class<?> cls) {
        List<Class<?>> classes = DescriptorUtil.getAllAssignableFrom(cls);
        ClassDefaults classDefaults = null;
        for (Class<?> c : classes)
            for (DescriptorExtractor extractor : extractors)
                classDefaults = DescriptorUtil.merge(classDefaults, extractor.extractClassDefaults(c));
        return classDefaults;
    }

}
