package org.zenframework.easyservices.test.dynamic;

import org.zenframework.easyservices.ValueTransfer;
import org.zenframework.easyservices.annotations.Value;
import org.zenframework.easyservices.test.simple.Function;

public interface Calculator {

    @Value(transfer = ValueTransfer.REF)
    Function getFunction(String name);

    int call(@Value(transfer = ValueTransfer.REF) Function function, int a, int b);

}
