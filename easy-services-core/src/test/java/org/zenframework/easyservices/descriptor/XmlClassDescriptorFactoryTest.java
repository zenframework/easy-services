package org.zenframework.easyservices.descriptor;

import org.zenframework.easyservices.ValueTransfer;

import junit.framework.TestCase;

public class XmlClassDescriptorFactoryTest extends TestCase {

    public void testXmlClassDescriptorFactory() throws Exception {
        XmlClassDescriptorFactory factory = new XmlClassDescriptorFactory("classpath://descriptor.xml");
        System.out.println(factory.getClassDescriptor(ServiceFactory.class));
        System.out.println(factory.getClassDescriptor(Service.class));
        assertTrue(factory.getClassDescriptor(ServiceFactory.class).getMethodDescriptor(ServiceFactory.class.getMethod("getService", String.class))
                .getReturnDescriptor().getTransfer() == ValueTransfer.REF);
    }

}
