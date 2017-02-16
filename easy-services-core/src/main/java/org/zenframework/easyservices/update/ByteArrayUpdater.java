package org.zenframework.easyservices.update;

import java.util.Arrays;

public class ByteArrayUpdater implements UpdateAdapter<byte[]> {

    @Override
    public Class<byte[]> getValueClass() {
        return byte[].class;
    }

    @Override
    public void update(byte[] oldValue, byte[] newValue, ValueUpdater updater) {
        if (oldValue == newValue || newValue == null)
            return;
        if (oldValue == null)
            throw new UpdateException("Old value is null");
        System.arraycopy(newValue, 0, oldValue, 0, newValue.length);
        if (oldValue.length > newValue.length)
            Arrays.fill(oldValue, newValue.length, oldValue.length, (byte) 0);
    }

}
