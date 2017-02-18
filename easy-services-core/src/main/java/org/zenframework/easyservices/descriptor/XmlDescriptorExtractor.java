package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zenframework.easyservices.ValueTransfer;

/**
 * Parses XML-based class descriptors
 * XML structure (all elements are optional, all attributes are required):
 * 
 * <classes>
 *
 *     <!-- Default value descriptors can be defined in class/value tag ... -->
 *     <class name="org.example.model.User">
 *         <value>
 *             <adapter>org.example.adapter.UserAdapter</adapter>
 *         </value>
 *     </class>
 *     <class name="org.example.service.Session">
 *         <value>
 *             <transfer>ref</transfer>
 *         </value>
 *     </class>
 * 
 *     <!-- ... or value descriptors can be defined in class/method tag ... -->
 *     <class name="org.example.service.UserManager">
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
 *                 <transfer>ref</transfer>
 *             </return>
 *         </method>
 *         <!-- returns reference to org.example.service.Session service -->
 *         <method name="login" arg-types="org.example.model.Token">
 *             <alias>loginToken</alias>
 *             <return>
 *                 <transfer>ref</transfer>
 *             </return>
 *         </method>
 *         <!-- returns java.util.Map<java.lang.String, org.example.model.User> -->
 *         <method name="getUsers" arg-types="">
 *             <return>
 *                 <type-parameters>java.lang.String, org.example.model.User</type-parameters>
 *             </return>
 *         </method>
 *     </class>
 *     
 * </classes>
 * 
 * @author Oleg S. Lekshin
 *
 */
public class XmlDescriptorExtractor implements DescriptorExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(XmlDescriptorExtractor.class);

    public static final String ELEM_CLASSES = "classes";
    public static final String ELEM_CLASS = "class";
    public static final String ELEM_VALUE = "value";
    public static final String ELEM_METHOD = "method";
    public static final String ELEM_ALIAS = "alias";
    public static final String ELEM_DEBUG = "debug";
    public static final String ELEM_RETURN = "return";
    public static final String ELEM_ARGUMENT = "arg";
    public static final String ELEM_ADAPTER = "adapter";
    public static final String ELEM_TYPE_PARAMETERS = "type-parameters";
    public static final String ELEM_TRANSFER = "transfer";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_ARG_TYPES = "arg-types";
    public static final String ATTR_NUMBER = "number";

    private final Map<Class<?>, ClassDescriptor> classes = new HashMap<Class<?>, ClassDescriptor>();
    private final Map<MethodIdentifier, MethodDescriptor> methods = new HashMap<MethodIdentifier, MethodDescriptor>();

    public XmlDescriptorExtractor(String... urls) {
        this(getUrls(urls));
    }

    public XmlDescriptorExtractor(URL... urls) {

        try {

            for (URL url : urls) {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(url.openStream());
                Element rootElement = doc.getDocumentElement();

                // read xml
                Enumeration<Element> classElements = getElements(rootElement, ELEM_CLASS);
                while (classElements.hasMoreElements()) {
                    Element classElement = classElements.nextElement();
                    Class<?> cls = getClass(getAttribute(classElement, ATTR_NAME, true));
                    ClassDescriptor classDescriptor = new ClassDescriptor();
                    Element valueElement = getElement(classElement, ELEM_VALUE);
                    if (valueElement != null) {
                        ValueDescriptor valueDescriptor = getValueDescriptor(valueElement);
                        classDescriptor.setValueDescriptor(valueDescriptor);
                    }
                    Element debugElement = getElement(classElement, ELEM_DEBUG);
                    if (debugElement != null)
                        classDescriptor.setDebug(Boolean.parseBoolean(debugElement.getTextContent()));
                    Enumeration<Element> methodElements = getElements(classElement, ELEM_METHOD);
                    while (methodElements.hasMoreElements()) {
                        Element methodElement = methodElements.nextElement();
                        String methodName = getAttribute(methodElement, ATTR_NAME, true);
                        Class<?>[] paramTypes = getClasses(getAttribute(methodElement, ATTR_ARG_TYPES, false));
                        try {
                            Method method = cls.getMethod(methodName, paramTypes);
                            MethodIdentifier methodIdentifier = new MethodIdentifier(method);
                            MethodDescriptor methodDescriptor = getMethodDescriptor(methodElement, paramTypes, method.getReturnType());
                            methods.put(methodIdentifier, DescriptorUtil.merge(methods.get(methodIdentifier), methodDescriptor));
                        } catch (NoSuchMethodException e) {
                            throw new SAXException(e);
                        }
                    }
                    classes.put(cls, DescriptorUtil.merge(classes.get(cls), classDescriptor));
                }

                LOG.info("Load " + url + " Ok");

            }

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public MethodDescriptor getMethodDescriptor(MethodIdentifier methodId) {
        return methods.get(methodId);
    }

    @Override
    public ClassDescriptor getClassDescriptor(Class<?> cls) {
        return classes.get(cls);
    }

    private static MethodDescriptor getMethodDescriptor(Element methodElement, Class<?>[] argTypes, Class<?> returnType) throws SAXException {
        MethodDescriptor methodDescriptor = new MethodDescriptor(argTypes.length);
        Element aliasElement = getElement(methodElement, ELEM_ALIAS);
        if (aliasElement != null)
            methodDescriptor.setAlias(aliasElement.getTextContent());
        Element returnElement = getElement(methodElement, ELEM_RETURN);
        if (returnElement != null) {
            ValueDescriptor returnDescriptor = getValueDescriptor(returnElement);
            methodDescriptor.setReturnDescriptor(returnDescriptor);
        }
        Element debugElement = getElement(methodElement, ELEM_DEBUG);
        if (debugElement != null)
            methodDescriptor.setDebug(Boolean.parseBoolean(debugElement.getTextContent()));
        Enumeration<Element> argElements = getElements(methodElement, ELEM_ARGUMENT);
        while (argElements.hasMoreElements()) {
            Element argElement = argElements.nextElement();
            ValueDescriptor argDescriptor = getValueDescriptor(argElement);
            methodDescriptor.setParameterDescriptor(Integer.parseInt(getAttribute(argElement, ATTR_NUMBER, true)), argDescriptor);
        }
        return methodDescriptor;
    }

    private static Element getElement(Element element, String name) throws SAXException {
        NodeList nodes = element.getElementsByTagName(name);
        if (nodes.getLength() > 1)
            throw new SAXException("Multiple '" + element.getNodeName() + "/" + name + "' elements found");
        return (Element) nodes.item(0);
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
        try {
            return ClassUtils.getClass(value);
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
        Element transferElement = getElement(valueElement, ELEM_TRANSFER);
        if (transferElement != null)
            valueDescriptor.setTransfer(ValueTransfer.forName(transferElement.getTextContent()));
        return valueDescriptor;
    }

    private static URL[] getUrls(String... urls) {
        URL[] result = new URL[urls.length];
        try {
            for (int i = 0; i < urls.length; i++)
                result[i] = new URL(urls[i]);
            return result;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
