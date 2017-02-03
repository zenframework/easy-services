package org.zenframework.easyservices.descriptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
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

/**
 * Parses XML-based services and default values descriptors
 * XML structure (all elements are optional, all attributes are required):
 * <[root]>
 *
 *     <!-- Value descriptors can be defined in <service> tag ... -->
 *     <service class="org.example.service.UserManager">
 *         <!-- returns void -->
 *         <method name="register" arg-types="org.example.model.User">
 *             <arg number="0">
 *                 <adapter>org.example.adapter.UserAdapter</adapter>
 *             </arg>
 *         </method>
 *         <!-- returns reference to org.example.service.Session service -->
 *         <method name="login" arg-types="java.lang.String,java.lang.String">
 *             <alias>loginUserPassword</alias>
 *             <return>
 *                 <reference>true</reference>
 *             </return>
 *         </method>
 *         <!-- returns reference to org.example.service.Session service -->
 *         <method name="login" arg-types="org.example.model.Token">
 *             <alias>loginToken</alias>
 *             <return>
 *                 <reference>true</reference>
 *             </return>
 *         </method>
 *         <!-- returns java.util.Map<java.lang.String, org.example.model.User> -->
 *         <method name="getUsers" arg-types="">
 *             <return>
 *                 <type-parameters>java.lang.String, org.example.model.User</type-parameters>
 *             </return>
 *         </method>
 *     </service>
 *     
 *     <!-- ... or most configs can be done with <default> tag -->
 *     <default class="org.example.model.User">
 *         <adapter>org.example.adapter.UserAdapter</adapter>
 *     </default>
 *     <default class="org.example.service.Session">
 *         <reference>true</reference>
 *     </default>
 * 
 * </[root]>
 * @author Oleg S. Lekshin
 *
 */
public class XmlServiceDescriptorFactory implements ServiceDescriptorFactory {

    private static final Map<String, Class<?>> PRIMITIVES = getPrimitives();

    public static final String ELEM_SERVICE = "service";
    public static final String ELEM_DEFAULT = "default";
    public static final String ELEM_METHOD = "method";
    public static final String ELEM_ALIAS = "alias";
    public static final String ELEM_RETURN = "return";
    public static final String ELEM_ARGUMENT = "argument";
    public static final String ELEM_ADAPTER = "adapter";
    public static final String ELEM_TYPE_PARAMETERS = "type-parameters";
    public static final String ELEM_REFERENCE = "reference";
    public static final String ATTR_CLASS = "class";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_ARG_TYPES = "arg-types";
    public static final String ATTR_NUMBER = "number";

    private final Map<Class<?>, ServiceDescriptor> services = new HashMap<Class<?>, ServiceDescriptor>();
    private final Map<Class<?>, ValueDescriptor> defaults = new HashMap<Class<?>, ValueDescriptor>();

    public XmlServiceDescriptorFactory(String url) throws ParserConfigurationException, SAXException, IOException {
        this(new URL(url));
    }

    public XmlServiceDescriptorFactory(URL url) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(url.openStream());
        Element root = doc.getDocumentElement();

        // defaults
        Enumeration<Element> defaultElements = getElements(root, ELEM_DEFAULT);
        while (defaultElements.hasMoreElements()) {
            Element defaultElement = defaultElements.nextElement();
            Class<?> cls = getClass(getAttribute(defaultElement, ATTR_CLASS, true));
            defaults.put(cls, getValueDescriptor(defaultElement));
        }

        // services
        Enumeration<Element> serviceElements = getElements(root, ELEM_SERVICE);
        while (serviceElements.hasMoreElements()) {
            Element serviceElement = serviceElements.nextElement();
            Class<?> serviceClass = getClass(getAttribute(serviceElement, ATTR_CLASS, true));
            services.put(serviceClass, getServiceDescriptor(serviceElement, serviceClass));
        }

    }

    @Override
    public ServiceDescriptor getServiceDescriptor(Class<?> serviceClass) {
        return services.get(serviceClass.getCanonicalName());
    }

    private ServiceDescriptor getServiceDescriptor(Element serviceElement, Class<?> serviceClass) throws SAXException {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor();
        Enumeration<Element> methodElements = getElements(serviceElement, ELEM_METHOD);
        while (methodElements.hasMoreElements()) {
            Element methodElement = methodElements.nextElement();
            String methodName = getAttribute(methodElement, ATTR_NAME, true);
            Class<?>[] argTypes = getClasses(getAttribute(methodElement, ATTR_ARG_TYPES, false));
            try {
                Method method = serviceClass.getMethod(methodName, argTypes);
                MethodIdentifier methodIdentifier = new MethodIdentifier(methodName, argTypes);
                MethodDescriptor methodDescriptor = getMethodDescriptor(methodElement, argTypes, method.getReturnType());
                serviceDescriptor.getMethodDescriptors().put(methodIdentifier, methodDescriptor);
            } catch (NoSuchMethodException e) {
                throw new SAXException(e);
            }
        }
        return serviceDescriptor;
    }

    private MethodDescriptor getMethodDescriptor(Element methodElement, Class<?>[] argTypes, Class<?> returnType) throws SAXException {
        MethodDescriptor methodDescriptor = new MethodDescriptor(argTypes.length);
        Element aliasElement = getElement(methodElement, ELEM_ALIAS);
        if (aliasElement != null)
            methodDescriptor.setAlias(aliasElement.getTextContent());
        Element returnElement = getElement(methodElement, ELEM_RETURN);
        if (returnElement != null) {
            ValueDescriptor returnDescriptor = getValueDescriptor(returnElement);
            if (returnDescriptor == null)
                returnDescriptor = defaults.get(returnType);
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

    private static Class<?> getClass(String value) throws SAXException {
        Class<?> cls = null;
        value = value.trim();
        try {
            cls = PRIMITIVES.get(value);
            if (cls == null)
                cls = Class.forName(value);
            return cls;
        } catch (ClassNotFoundException e) {
            throw new SAXException(e);
        }
    }

    private static Class<?>[] getClasses(String value) throws SAXException {
        if (value == null || value.isEmpty())
            return new Class<?>[0];
        String[] classNames = value.split("\\,");
        Class<?>[] classes = new Class<?>[classNames.length];
        for (int i = 0; i < classNames.length; i++)
            classes[i] = getClass(classNames[i]);
        return classes;
    }

    private static Map<String, Class<?>> getPrimitives() {
        Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();
        for (Class<?> cls : Arrays.asList(byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class))
            primitives.put(cls.getName(), cls);
        return primitives;
    }

    private static ValueDescriptor getValueDescriptor(Element valueElement) throws SAXException {
        ValueDescriptor valueDescriptor = new ValueDescriptor();
        Enumeration<Element> adapterElements = getElements(valueElement, ELEM_ADAPTER);
        try {
            while (adapterElements.hasMoreElements())
                valueDescriptor.addAdapter(Class.forName(adapterElements.nextElement().getTextContent()).newInstance());
        } catch (Exception e) {
            throw new SAXException(e);
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
