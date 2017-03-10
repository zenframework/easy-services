package org.zenframework.easyservices.descriptor;

public class ParamDescriptor extends ValueDescriptor {

    private static final long serialVersionUID = 1L;

    private boolean close;

    public ParamDescriptor() {
        super();
    }

    public ParamDescriptor(ValueTransfer transfer, boolean close, Class<?>... typeParameters) {
        super(transfer, typeParameters);
        this.close = close;
    }

    public ParamDescriptor(ValueDescriptor valueDescriptor) {
        super(valueDescriptor.getTransfer(), valueDescriptor.getTypeParameters());
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

}
