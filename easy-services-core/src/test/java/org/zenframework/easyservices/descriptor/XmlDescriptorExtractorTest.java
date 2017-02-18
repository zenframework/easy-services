package org.zenframework.easyservices.descriptor;

import java.io.InputStream;

import org.zenframework.easyservices.ValueTransfer;

import junit.framework.TestCase;

public class XmlDescriptorExtractorTest extends TestCase {

    public void testXmlClassDescriptorFactory() throws Exception {
        XmlDescriptorExtractor factory = new XmlDescriptorExtractor("classpath://META-INF/easy-services/descriptor.xml");
        System.out.println(factory.getClassDescriptor(ServiceFactory.class));
        System.out.println(factory.getClassDescriptor(InputStream.class));
        assertEquals(ValueTransfer.REF, factory.getClassDescriptor(InputStream.class).getValueDescriptor().getTransfer());
        assertEquals(ValueTransfer.OUT, factory.getMethodDescriptor(new MethodIdentifier(InputStream.class.getMethod("read", byte[].class)))
                .getParameterDescriptor(0).getTransfer());
    }

}
