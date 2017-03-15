package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.serialize.Serializer;

public class InvocationContext {

    private final Method method;
    private final MethodDescriptor methodDescriptor;
    private final Serializer serializer;
    private final Object[] rawParams;
    private final Object[] params;

    public InvocationContext(Method method, MethodDescriptor methodDescriptor, Serializer serializer, Object[] rawParams, Object[] params)
            throws IOException, ServiceException {
        this.method = method;
        this.methodDescriptor = methodDescriptor;
        this.serializer = serializer;
        this.rawParams = rawParams;
        this.params = params;
    }

    public Method getMethod() {
        return method;
    }

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public Object[] getRawParams() {
        return rawParams;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(method);
        if (methodDescriptor != null) {
            String mdStr = methodDescriptor.toString();
            if (mdStr.length() > 0)
                str.append(", descriptor ").append(mdStr);
        }
        str.append(", serializer: ").append(serializer).append(", parameters: ").append(Arrays.toString(params));
        return str.toString();
    }

}
