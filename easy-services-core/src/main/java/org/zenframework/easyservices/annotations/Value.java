package org.zenframework.easyservices.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zenframework.easyservices.ValueTransfer;

@Target(value = { ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Value {

    Class<?>[] adapters() default {};

    Class<?>[] typeParameters() default {};

    ValueTransfer transfer() default ValueTransfer.DEFAULT;

}
