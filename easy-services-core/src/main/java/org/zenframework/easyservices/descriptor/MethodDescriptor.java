package org.zenframework.easyservices.descriptor;

import java.util.Map;

import org.zenframework.commons.string.StringUtil;

public class MethodDescriptor {

    private String alias = null;
    private ValueDescriptor returnDescriptor = null;
    private final ValueDescriptor[] parameterDescriptors;
    private boolean emptyParameterDescriptors = true;

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

    public void setParameterDescriptorsMap(Map<Integer, ValueDescriptor> paramDescriptorsMap) {
        for (int i = 0; i < parameterDescriptors.length; i++)
            setParameterDescriptor(i, paramDescriptorsMap.get(i));
    }

    public ValueDescriptor getParameterDescriptor(int arg) {
        return parameterDescriptors[arg];
    }

    public void setParameterDescriptor(int arg, ValueDescriptor paramDescriptor) {
        parameterDescriptors[arg] = paramDescriptor;
        if (paramDescriptor != null)
            emptyParameterDescriptors = false;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder str = new StringBuilder();
        str.append('[');
        if (alias != null)
            StringUtil.newLine(str, indent + 1).append("alias : ").append(alias);
        if (returnDescriptor != null)
            StringUtil.newLine(str, indent + 1).append("return : ").append(returnDescriptor);
        if (!emptyParameterDescriptors) {
            StringUtil.newLine(str, indent + 1).append("parameters : [");
            for (int i = 0; i < parameterDescriptors.length; i++) {
                if (parameterDescriptors[i] != null)
                    StringUtil.newLine(str, indent + 2).append(i).append(" : ").append(parameterDescriptors[i]);
            }
        }
        if (str.length() > 1)
            StringUtil.newLine(str, indent);
        return str.append(']').toString();
    }

}