package org.zenframework.easyservices.descriptor;

import java.util.Map;

public class MethodDescriptor {

    private String alias = null;
    private ValueDescriptor returnDescriptor = null;
    private final ValueDescriptor[] argumentDescriptors;

    public MethodDescriptor(int argsCount) {
        this.argumentDescriptors = new ValueDescriptor[argsCount];
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

    public ValueDescriptor[] getArgumentDescriptors() {
        return argumentDescriptors;
    }

    public void setArgumentDescriptorsMap(Map<Integer, ValueDescriptor> argumentDescriptorsMap) {
        for (int i = 0; i < argumentDescriptors.length; i++)
            argumentDescriptors[i] = argumentDescriptorsMap.get(i);
    }

    public ValueDescriptor getArgumentDescriptor(int arg) {
        return argumentDescriptors[arg];
    }

    public void setArgumentDescriptor(int arg, ValueDescriptor argDescriptor) {
        argumentDescriptors[arg] = argDescriptor;
    }

}