package org.zenframework.easyservices.descriptor;

import java.io.InputStream;

import junit.framework.TestCase;

public class XmlDescriptorExtractorTest extends TestCase {

    public void testXmlClassDescriptorFactory() throws Exception {
        XmlDescriptorExtractor extractor = new XmlDescriptorExtractor("classpath://META-INF/easy-services/descriptor.xml");
        System.out.println(extractor.extractClassDefaults(ServiceFactory.class));
        System.out.println(extractor.extractClassDefaults(InputStream.class));
        assertEquals(ValueTransfer.REF, extractor.extractClassDefaults(InputStream.class).getValueDescriptor().getTransfer());
        assertEquals(ValueTransfer.OUT,
                extractor.extractMethodDescriptor(InputStream.class, new MethodIdentifier(InputStream.class.getMethod("read", byte[].class)))
                        .getParameterDescriptor(0).getTransfer());
    }

}
