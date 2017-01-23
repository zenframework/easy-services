package org.zenframework.easyservices.descriptor;

import java.util.HashMap;
import java.util.Map;

public class MethodDescriptor {

    private String alias;
    private ValueDescriptor returnDescriptor;
    private final Map<Integer, ValueDescriptor> argumentDescriptors = new HashMap<Integer, ValueDescriptor>();

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

    public Map<Integer, ValueDescriptor> getArgumentDescriptors() {
        return argumentDescriptors;
    }

    public void setArgumentDescriptors(Map<Integer, ValueDescriptor> argumentDescriptors) {
        this.argumentDescriptors.clear();
        this.argumentDescriptors.putAll(argumentDescriptors);
    }

    public ValueDescriptor getArgumentDescriptor(int arg) {
        return argumentDescriptors.get(arg);
    }

}