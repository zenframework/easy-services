package org.zenframework.easyservices.descriptor;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang.ClassUtils;

public class ValueDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private ValueTransfer transfer = null;
    private Class<?>[] typeParameters = new Class<?>[0];

    public ValueDescriptor() {}

    public ValueDescriptor(ValueTransfer transfer, Class<?>... typeParameters) {
        this.transfer = transfer;
        this.typeParameters = typeParameters;
    }

    public Class<?>[] getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(Class<?>... typeParameters) {
        this.typeParameters = typeParameters;
    }

    public ValueTransfer getTransfer() {
        return transfer;
    }

    public void setTransfer(ValueTransfer transfer) {
        this.transfer = transfer;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append('[');
        if (transfer != null && transfer != ValueTransfer.DEFAULT)
            str.append("tr:").append(transfer).append(' ');
        if (typeParameters != null && typeParameters.length > 0)
            str.append("tp:").append(ClassUtils.convertClassesToClassNames(Arrays.asList(typeParameters))).append(' ');
        if (str.length() > 1)
            str.setLength(str.length() - 1);
        return str.append(']').toString();
    }

}
