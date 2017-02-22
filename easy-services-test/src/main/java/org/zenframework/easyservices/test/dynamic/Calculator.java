package org.zenframework.easyservices.test.dynamic;

import org.zenframework.easyservices.annotations.Close;
import org.zenframework.easyservices.annotations.Ref;
import org.zenframework.easyservices.test.simple.Function;

public interface Calculator {

    @Ref
    Function getFunction(String name);

    void close(@Ref @Close Function function);

    int call(@Ref Function function, int a, int b);

}
