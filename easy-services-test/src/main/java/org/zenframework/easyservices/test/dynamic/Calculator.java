package org.zenframework.easyservices.test.dynamic;

import org.zenframework.easyservices.annotations.Value;
import org.zenframework.easyservices.test.simple.Function;

public interface Calculator {

    @Value(dynamicService = true)
    Function getFunction(String name);

    int call(@Value(dynamicService = true) Function function, int a, int b);

}
