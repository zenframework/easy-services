package org.zenframework.easyservices.descriptor;

public class ClassDefaults {

    private ValueDescriptor valueDescriptor;
    private Boolean debug;

    public ClassDefaults() {}

    public ClassDefaults(ValueDescriptor valueDescriptor, Boolean debug) {
        this.valueDescriptor = valueDescriptor;
        this.debug = debug;
    }

    public ValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

    public void setValueDescriptor(ValueDescriptor valueDescriptor) {
        this.valueDescriptor = valueDescriptor;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

}
