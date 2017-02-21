package org.zenframework.easyservices.test.dynamic;

import org.zenframework.easyservices.annotations.Ref;
import org.zenframework.easyservices.test.simple.Function;

public interface Calculator {

    @Ref
    Function getFunction(String name);

    int call(@Ref Function function, int a, int b);

}
