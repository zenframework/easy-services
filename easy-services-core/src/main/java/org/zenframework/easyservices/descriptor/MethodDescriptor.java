package org.zenframework.easyservices.descriptor;

import java.io.Serializable;
import java.util.Map;

public class MethodDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String alias = null;
    private ValueDescriptor returnDescriptor = null;
    private final ValueDescriptor[] parameterDescriptors;
    private boolean emptyParameterDescriptors = true;
    private Boolean debug = false;

    public MethodDescriptor(int argsCount) {
        this.parameterDescriptors = new ValueDescriptor[argsCount];
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ValueDescriptor getReturnDescriptor() {
        return returnDescriptor;
    }

    public void setReturnDescriptor(ValueDescriptor returnDescriptor) {
        this.returnDescriptor = returnDescriptor;
    }

    public ValueDescriptor[] getParameterDescriptors() {
        return parameterDescriptors;
    }

    public void setParameterDescriptors(ValueDescriptor[] paramDescriptors) {
        for (int i = 0; i < parameterDescriptors.length; i++)
            setParameterDescriptor(i, paramDescriptors != null ? paramDescriptors[i] : null);
    }

    public void setParameterDescriptorsMap(Map<Integer, ValueDescriptor> paramDescriptorsMap) {
        for (int i = 0; i < parameterDescriptors.length; i++)
            setParameterDescriptor(i, paramDescriptorsMap != null ? paramDescriptorsMap.get(i) : null);
    }

    public ValueDescriptor getParameterDescriptor(int arg) {
        return parameterDescriptors[arg];
    }

    public void setParameterDescriptor(int arg, ValueDescriptor paramDescriptor) {
        parameterDescriptors[arg] = paramDescriptor;
        if (paramDescriptor != null)
            emptyParameterDescriptors = false;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public boolean isEmpty() {
        return alias == null && returnDescriptor == null && emptyParameterDescriptors;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append('[');
        if (alias != null)
            str.append("alias: ").append(alias).append(", ");
        if (!emptyParameterDescriptors) {
            str.append("params: [");
            for (int i = 0; i < parameterDescriptors.length; i++) {
                str.append(parameterDescriptors[i] != null ? parameterDescriptors[i] : "-");
                if (i < parameterDescriptors.length - 1)
                    str.append(", ");
            }
            str.append("], ");
        }
        if (returnDescriptor != null)
            str.append("return: ").append(returnDescriptor).append(", ");
        if (str.length() > 1)
            str.setLength(str.length() - 2);
        str.append(']');
        return str.toString();
    }

}