package org.zenframework.easyservices.descriptor;

import org.zenframework.easyservices.ValueTransfer;
import org.zenframework.easyservices.annotations.Value;

@Value(transfer = ValueTransfer.REF)
public interface Service {

    void call();

}
