package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.MethodDescriptor;

public interface SerializerFactory {

    String getFormat();

    Serializer getSerializer(Class<?>[] paramTypes, Class<?> returnType, MethodDescriptor methodDescriptor);

}
