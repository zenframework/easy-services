package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.commons.string.StringUtil;

public class ClassDescriptor {

    private final Map<MethodIdentifier, MethodDescriptor> methodDescriptors = new HashMap<MethodIdentifier, MethodDescriptor>();
    private ValueDescriptor valueDescriptor;
    private boolean debug = false;

    public Map<MethodIdentifier, MethodDescriptor> getMethodDescriptors() {
        return methodDescriptors;
    }

    public void setMethodDescriptors(Map<MethodIdentifier, MethodDescriptor> methodDescriptors) {
        this.methodDescriptors.clear();
        this.methodDescriptors.putAll(methodDescriptors);
    }

    public MethodDescriptor getMethodDescriptor(Method method) {
        return methodDescriptors.get(new MethodIdentifier(method));
    }

    public void setMethodDescriptor(MethodIdentifier methodIdentifier, MethodDescriptor methodDescriptor) {
        this.methodDescriptors.put(methodIdentifier, methodDescriptor);
    }

    public ValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

    public void setValueDescriptor(ValueDescriptor valueDescriptor) {
        this.valueDescriptor = valueDescriptor;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder str = new StringBuilder();
        str.append('[');
        if (valueDescriptor != null)
            StringUtil.newLine(str, indent + 1).append("value : ").append(valueDescriptor);
        if (!methodDescriptors.isEmpty()) {
            StringUtil.newLine(str, indent + 1).append("methods :");
            for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : methodDescriptors.entrySet()) {
                StringUtil.newLine(str, indent + 2).append(entry.getKey()).append(" : ").append(entry.getValue().toString(indent + 2));
            }
        }
        if (valueDescriptor != null || !methodDescriptors.isEmpty())
            StringUtil.newLine(str, indent);
        return str.append(']').toString();
    }

}
