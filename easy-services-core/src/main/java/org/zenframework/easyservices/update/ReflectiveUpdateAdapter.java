package org.zenframework.easyservices.update;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectiveUpdateAdapter implements UpdateAdapter<Object> {

    @Override
    public Class<Object> getValueClass() {
        return Object.class;
    }

    @Override
    public void update(Object oldValue, Object newValue, ValueUpdater updater) {
        Class<?> cls = oldValue.getClass();
        try {
            for (Field field : cls.getFields())
                field.set(oldValue, field.get(newValue));
            for (Method method : cls.getMethods()) {
                String name = method.getName();
                Class<?>[] paramTypes = method.getParameterTypes();
                // if method is setter
                if (name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(3)) && method.getReturnType() == void.class
                        && paramTypes.length == 1) {
                    try {
                        // try to find and invoke getter
                        Method getter = cls.getMethod("get" + name.substring(3));
                        method.invoke(oldValue, getter.invoke(newValue));
                    } catch (Throwable e) {}
                }
            }
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
