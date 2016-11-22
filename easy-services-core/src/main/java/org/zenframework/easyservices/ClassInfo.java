package org.zenframework.easyservices;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public class ClassInfo {

    public static final String TYPE = "type";
    public static final String ENUM = "enum";
    public static final String CLASS = "class";
    public static final String METHODS = "methods";
    public static final String ARGUMENTS = "arguments";
    public static final String RETURNS = "returns";

    private static final Map<Class<?>, String> SIMPLE_MAPPER = getSimpleClassesMapper();
    private static final Map<Class<?>, String> INTERFACE_MAPPER = getInterfacesMapper();

    private ClassInfo() {}

    public static List<Method> getMethods(Object service) {
        List<Method> methods = new LinkedList<Method>();
        for (Class<?> clazz : service.getClass().getInterfaces()) {
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        }
        return methods;
    }

    public static Map<String, Class<?>> getFields(Class<?> clazz) {
        Map<String, Class<?>> fields = new HashMap<String, Class<?>>(clazz.getMethods().length);
        for (Field field : clazz.getFields())
            fields.put(field.getName(), field.getType());
        for (Method method : clazz.getMethods()) {
            String fieldName = extractFieldNameFromGetter(clazz, method);
            if (fieldName != null)
                fields.put(fieldName, method.getReturnType());
        }
        return fields;
    }

    public static String extractFieldNameFromGetter(Class<?> clazz, Method method) {
        String getter = method.getName();
        // check if getter begins with 'get' and has no args
        if (method.getTypeParameters().length > 0 || getter.length() < 4 || !getter.startsWith("get")) {
            return null;
        }
        // check if the 4th letter is in upper case
        String firstLetter = getter.substring(3, 4);
        if (!firstLetter.toUpperCase().equals(firstLetter)) {
            return null;
        }
        // check corresponding setter exists
        try {
            if (clazz.getMethod("set" + getter.substring(3), method.getReturnType()).getReturnType() != void.class) {
                return null;
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
        return firstLetter.toLowerCase() + getter.substring(4);
    }

    public static String mapSimpleClass(Class<?> clazz) {
        if (clazz.isPrimitive())
            return clazz.getName();
        String mapped = SIMPLE_MAPPER.get(clazz);
        if (mapped != null)
            return mapped;
        for (Entry<Class<?>, String> interfaceMapping : INTERFACE_MAPPER.entrySet())
            if (interfaceMapping.getKey().isAssignableFrom(clazz))
                return interfaceMapping.getValue();
        return null;
    }

    public static String toArray(int arrayDeep, String... names) {
        StringBuilder str = new StringBuilder(100);
        for (int i = 0; i < arrayDeep; i++) {
            str.append("[]");
        }
        String arr = str.toString();
        str.setLength(0);
        for (String name : names)
            str.append(name).append(arr).append('/');
        if (str.length() > 0)
            str.setLength(str.length() - 1);
        return str.toString();
    }

    private static Map<Class<?>, String> getSimpleClassesMapper() {
        Map<Class<?>, String> mapper = new HashMap<Class<?>, String>();
        mapper.put(Byte.class, byte.class.getName());
        mapper.put(Character.class, char.class.getName());
        mapper.put(Short.class, short.class.getName());
        mapper.put(Integer.class, int.class.getName());
        mapper.put(Long.class, long.class.getName());
        mapper.put(Boolean.class, boolean.class.getName());
        mapper.put(Float.class, float.class.getName());
        mapper.put(Double.class, double.class.getName());
        mapper.put(String.class, "string");
        mapper.put(URL.class, "string/url");
        mapper.put(URI.class, "string/uri");
        mapper.put(UUID.class, "string/uuid");
        return mapper;
    }

    private static Map<Class<?>, String> getInterfacesMapper() {
        Map<Class<?>, String> mapper = new HashMap<Class<?>, String>();
        mapper.put(Collection.class, "collection");
        mapper.put(Map.class, "dictionary");
        return mapper;
    }

}
