package org.zenframework.easyservices.descriptor;

import java.util.Map;

public class MethodDescriptor {

    private String alias = null;
    private ValueDescriptor returnDescriptor = null;
    private final ValueDescriptor[] parameterDescriptors;

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

    public void setArgumentDescriptorsMap(Map<Integer, ValueDescriptor> paramDescriptorsMap) {
        for (int i = 0; i < parameterDescriptors.length; i++)
            parameterDescriptors[i] = paramDescriptorsMap.get(i);
    }

    public ValueDescriptor getArgumentDescriptor(int arg) {
        return parameterDescriptors[arg];
    }

    public void setParameterDescriptor(int arg, ValueDescriptor paramDescriptor) {
        parameterDescriptors[arg] = paramDescriptor;
    }

}