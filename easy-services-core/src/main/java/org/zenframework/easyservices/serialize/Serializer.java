package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.descriptor.ValueDescriptor;

public interface Serializer {

    <T> T deserialize(String data, Class<T> objType) throws SerializationException;

    <T> T deserialize(String data, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException;

    Object[] deserialize(String data, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws ServiceException;

    String serialize(Object object);

}
