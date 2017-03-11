package org.zenframework.easyservices.bean;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DefaultObjectConverter implements ObjectConverter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Override
    public Object toClass(Object value, Class<?> clazz) throws ConverterException {
        if (clazz == byte.class || clazz == Byte.class)
            return Byte.valueOf(value.toString());
        if (clazz == short.class || clazz == Short.class)
            return Short.valueOf(value.toString());
        if (clazz == int.class || clazz == Integer.class)
            return Integer.valueOf(value.toString());
        if (clazz == long.class || clazz == Long.class)
            return Long.valueOf(value.toString());
        if (clazz == float.class || clazz == Float.class)
            return Float.valueOf(value.toString());
        if (clazz == double.class || clazz == Double.class)
            return Double.valueOf(value.toString());
        if (clazz == char.class || clazz == Character.class) {
            if (value.toString().length() != 1)
                throw new ConverterException(value, clazz);
            return value.toString().charAt(0);
        }
        if (clazz == boolean.class || clazz == Boolean.class)
            return Boolean.valueOf(value.toString());
        if (clazz == String.class)
            return value.toString();
        if (clazz == UUID.class)
            return UUID.fromString(value.toString());
        if (clazz == Date.class) {
            try {
                return DATE_FORMAT.parse(value.toString());
            } catch (ParseException e) {
                throw new ConverterException(value, clazz, e);
            }
        }
        throw new ConverterException(value, clazz);
    }

}
