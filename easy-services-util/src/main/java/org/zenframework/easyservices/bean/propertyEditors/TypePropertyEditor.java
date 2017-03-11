package org.zenframework.easyservices.bean.propertyEditors;

import java.beans.PropertyEditorSupport;

public class TypePropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(Class.forName(text));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
