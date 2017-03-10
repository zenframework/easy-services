package org.zenframework.easyservices.descriptor;

import java.io.InputStream;
import java.lang.reflect.Method;

import org.zenframework.easyservices.Environment;

import junit.framework.TestCase;

public class DefaultDescriptorFactoryTest extends TestCase {

    public void testAutoAliasing() throws Exception {
        Environment.setAutoAliasing(true);
        DescriptorFactory factory = new DefaultDescriptorFactory();
        ClassDescriptor classDescriptor = factory.getClassDescriptor(InputStream.class);
        for (Method method : InputStream.class.getMethods()) {
            MethodIdentifier methodId = new MethodIdentifier(method);
            MethodDescriptor methodDescriptor = classDescriptor.getMethodDescriptor(methodId);
            boolean repeat = method.getName().equals("read") || method.getName().equals("wait");
            boolean hasAlias = methodDescriptor != null && methodDescriptor.getAlias() != null;
            System.out.println(methodId + " : " + (hasAlias ? methodDescriptor.getAlias() : "---"));
            assertEquals(repeat, hasAlias);
        }
    }

    public void testExtractClassDescriptor() throws Exception {
        DefaultDescriptorFactory factory = new DefaultDescriptorFactory();
        System.out.println(ServiceFactory.class.getCanonicalName() + ": " + factory.getClassDescriptor(ServiceFactory.class));
        System.out.println(InputStream.class.getCanonicalName() + ": " + factory.getClassDescriptor(InputStream.class));
        System.out.println(Service.class.getCanonicalName() + ": " + factory.getClassDescriptor(Service.class));
        assertTrue(factory.getClassDescriptor(ServiceFactory.class)
                .getMethodDescriptor(new MethodIdentifier(ServiceFactory.class.getMethod("getInputStream"))).getReturnDescriptor()
                .getTransfer() == ValueTransfer.REF);
        assertTrue(factory.getClassDescriptor(ServiceFactory.class)
                .getMethodDescriptor(new MethodIdentifier(ServiceFactory.class.getMethod("getService", String.class))).getReturnDescriptor()
                .getTransfer() == ValueTransfer.REF);
    }

}
