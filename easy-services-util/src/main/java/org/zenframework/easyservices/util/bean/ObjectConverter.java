package org.zenframework.easyservices.util.bean;

public interface ObjectConverter {

    Object toClass(Object value, Class<?> clazz) throws ConverterException;

}
