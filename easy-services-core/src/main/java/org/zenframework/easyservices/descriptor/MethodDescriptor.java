package org.zenframework.easyservices.descriptor;

import java.io.Serializable;
import java.util.Map;

import org.zenframework.easyservices.util.string.StringUtil;

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

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuilder str = new StringBuilder();
        if (alias != null)
            StringUtil.indent(str, indent, false).append("alias : ").append(alias);
        if (returnDescriptor != null)
            StringUtil.indent(str, indent, str.length() > 0).append("return : ").append(returnDescriptor);
        if (!emptyParameterDescriptors) {
            StringUtil.indent(str, indent, str.length() > 0).append("parameters :");
            for (int i = 0; i < parameterDescriptors.length; i++) {
                if (parameterDescriptors[i] != null)
                    StringUtil.indent(str, indent + 1, true).append(i).append(" : ").append(parameterDescriptors[i]);
            }

        }
        return str.toString();
    }

}