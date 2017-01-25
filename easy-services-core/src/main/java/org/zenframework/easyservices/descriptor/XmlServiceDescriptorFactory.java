package org.zenframework.easyservices.descriptor;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zenframework.easyservices.serialize.SerializerAdapter;

public class XmlServiceDescriptorFactory implements ServiceDescriptorFactory {

    public static final String ELEM_SERVICE = "service";
    public static final String ELEM_METHOD = "method";
    public static final String ELEM_ALIAS = "alias";
    public static final String ELEM_RETURN = "return";
    public static final String ELEM_ARGUMENT = "argument";
    public static final String ELEM_SERIALIZER_ADAPTER = "serializer-adapter";
    public static final String ELEM_TYPE_PARAMETERS = "type-parameters";
    public static final String ELEM_REFERENCE = "reference";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_ARG_TYPES = "arg-types";
    public static final String ATTR_NUMBER = "number";

    private final Map<String, ServiceDescriptor> descriptors = new HashMap<String, ServiceDescriptor>();

    public XmlServiceDescriptorFactory(String url) throws ParserConfigurationException, SAXException, IOException {
        this(new URL(url));
    }

    public XmlServiceDescriptorFactory(URL url) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(url.openStream());
        Element root = doc.getDocumentElement();
        Enumeration<Element> serviceElements = getElements(root, ELEM_SERVICE);
        while (serviceElements.hasMoreElements()) {
            Element serviceElement = serviceElements.nextElement();
            String serviceName = getAttribute(serviceElement, ATTR_NAME, true);
            descriptors.put(serviceName, getServiceDescriptor(serviceElement));
        }
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(Class<?> serviceClass) {
        return descriptors.get(serviceClass.getCanonicalName());
    }

    private static Element getElement(Element element, String name) {
        NodeList nodes = element.getElementsByTagName(name);
        if (nodes.getLength() > 0)
            return (Element) nodes.item(0);
        return null;
    }

    private static Enumeration<Element> getElements(Element element, String name) {
        final NodeList nodes = element.getElementsByTagName(name);
        return new Enumeration<Element>() {

            private int n = 0;

            @Override
            public boolean hasMoreElements() {
                return n < nodes.getLength();
            }

            @Override
            public Element nextElement() {
                return (Element) nodes.item(n++);
            }

        };
    }

    private static String getAttribute(Element element, String name, boolean required) throws SAXException {
        String value = element.getAttribute(name);
        if (value == null && required)
            throw new SAXException("Element '" + element + "': attribute '" + name + "' is required");
        return value;
    }

    private static Class<?>[] getClasses(String value) throws SAXException {
        if (value == null || value.isEmpty())
            return new Class<?>[0];
        String[] classNames = value.split("\\,");
        Class<?>[] classes = new Class<?>[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            try {
                classes[i] = Class.forName(classNames[i]);
            } catch (ClassNotFoundException e) {
                throw new SAXException(e);
            }
        }
        return classes;
    }

    private static ServiceDescriptor getServiceDescriptor(Element serviceElement) throws SAXException {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor();
        Enumeration<Element> methodElements = getElements(serviceElement, ELEM_METHOD);
        while (methodElements.hasMoreElements()) {
            Element methodElement = methodElements.nextElement();
            Class<?>[] argTypes = getClasses(getAttribute(methodElement, ATTR_ARG_TYPES, false));
            MethodIdentifier methodIdentifier = new MethodIdentifier(getAttribute(methodElement, ATTR_NAME, true), argTypes);
            MethodDescriptor methodDescriptor = getMethodDescriptor(methodElement, argTypes.length);
            serviceDescriptor.getMethodDescriptors().put(methodIdentifier, methodDescriptor);
        }
        return serviceDescriptor;
    }

    private static MethodDescriptor getMethodDescriptor(Element methodElement, int argsCount) throws SAXException {
        MethodDescriptor methodDescriptor = new MethodDescriptor(argsCount);
        Element aliasElement = getElement(methodElement, ELEM_ALIAS);
        if (aliasElement != null)
            methodDescriptor.setAlias(aliasElement.getTextContent());
        Element returnElement = getElement(methodElement, ELEM_RETURN);
        if (returnElement != null) {
            ValueDescriptor returnDescriptor = getValueDescriptor(returnElement);
            methodDescriptor.setReturnDescriptor(returnDescriptor);
        }
        Enumeration<Element> argElements = getElements(methodElement, ELEM_ARGUMENT);
        while (argElements.hasMoreElements()) {
            Element argElement = argElements.nextElement();
            ValueDescriptor argDescriptor = getValueDescriptor(argElement);
            methodDescriptor.setArgumentDescriptor(Integer.parseInt(getAttribute(argElement, ATTR_NUMBER, true)), argDescriptor);
        }
        return methodDescriptor;
    }

    private static ValueDescriptor getValueDescriptor(Element valueElement) throws SAXException {
        ValueDescriptor valueDescriptor = new ValueDescriptor();
        Element serializerAdapterElement = getElement(valueElement, ELEM_SERIALIZER_ADAPTER);
        if (serializerAdapterElement != null) {
            try {
                valueDescriptor
                        .setSerializerAdapter((SerializerAdapter<?, ?>) Class.forName(serializerAdapterElement.getTextContent()).newInstance());
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
        Element typeParametersElement = getElement(valueElement, ELEM_TYPE_PARAMETERS);
        if (typeParametersElement != null)
            valueDescriptor.setTypeParameters(getClasses(typeParametersElement.getTextContent()));
        Element referenceElement = getElement(valueElement, ELEM_REFERENCE);
        if (referenceElement != null)
            valueDescriptor.setReference(Boolean.parseBoolean(referenceElement.getTextContent()));
        return valueDescriptor;
    }

}
