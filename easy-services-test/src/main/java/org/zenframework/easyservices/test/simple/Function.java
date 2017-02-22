package org.zenframework.easyservices.test.simple;

import org.zenframework.easyservices.annotations.Close;

public interface Function {

    int call(int a, int b);

    @Close
    void close();

}
