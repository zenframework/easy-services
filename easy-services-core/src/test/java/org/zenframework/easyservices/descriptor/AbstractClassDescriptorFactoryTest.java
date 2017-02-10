package org.zenframework.easyservices.descriptor;

import java.io.InputStream;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class AbstractClassDescriptorFactoryTest extends TestCase {

    public void testAutoAliasing() throws Exception {
        ClassDescriptorFactory factory = new AbstractClassDescriptorFactory() {

            @Override
            protected ClassDescriptor extractClassDescriptor(Class<?> cls) {
                return null;
            }

        };
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

}
