package org.zenframework.easyservices.descriptor;

import java.io.Serializable;

public class ClassDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private ValueDescriptor valueDescriptor = null;
    private Boolean debug = false;

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
