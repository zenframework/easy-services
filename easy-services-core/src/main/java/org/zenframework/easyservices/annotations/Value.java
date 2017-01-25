package org.zenframework.easyservices.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.METHOD, ElementType.PARAMETER })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Value {

    @SuppressWarnings("rawtypes")
    Class<? extends org.zenframework.easyservices.serialize.SerializerAdapter> serializerAdapter() default org.zenframework.easyservices.serialize.SerializerAdapter.class;

    Class<?>[] typeParameters() default {};

    boolean reference() default false;

}
