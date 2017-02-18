package org.zenframework.easyservices.serialize.json.gson;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.util.cls.ClassInfo;
import org.zenframework.easyservices.util.cls.ClassRef;
import org.zenframework.easyservices.util.cls.FieldInfo;
import org.zenframework.easyservices.util.cls.MethodInfo;

import com.google.gson.TypeAdapter;

public class DefaultTypeAdapterFactory extends SimpleTypeAdapterFactory {

    public DefaultTypeAdapterFactory() {
        super(getDefaultTypeAdapters());
    }

    private static Map<Class<?>, TypeAdapter<?>> getDefaultTypeAdapters() {
        Map<Class<?>, TypeAdapter<?>> typeAdapters = new HashMap<Class<?>, TypeAdapter<?>>();
        typeAdapters.put(byte[].class, new ByteArrayTypeAdapter());
        typeAdapters.put(Throwable.class, new ThrowableTypeAdapter());
        typeAdapters.put(Date.class, new DateStringTypeAdapter());
        typeAdapters.put(Locale.class, new LocaleTypeAdapter());
        typeAdapters.put(Class.class, new ClassTypeAdapter());
        typeAdapters.put(URI.class, new URITypeAdapter());
        typeAdapters.put(ClassInfo.class, new ClassInfoTypeAdapter());
        typeAdapters.put(ClassRef.class, new ClassRefTypeAdapter());
        typeAdapters.put(FieldInfo.class, new FieldInfoTypeAdapter());
        typeAdapters.put(MethodInfo.class, new MethodInfoTypeAdapter());
        typeAdapters.put(ValueDescriptor.class, new ValueDescriptorTypeAdapter());
        typeAdapters.put(MethodIdentifier.class, new MethodIdentifierTypeAdapter());
        typeAdapters.put(MethodDescriptor.class, new MethodDescriptorTypeAdapter());
        typeAdapters.put(ClassDescriptor.class, new ClassDescriptorTypeAdapter());
        return typeAdapters;
    }

}
