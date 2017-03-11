package org.zenframework.easyservices.bean;

public interface ObjectConverter {

    Object toClass(Object value, Class<?> clazz) throws ConverterException;

}
