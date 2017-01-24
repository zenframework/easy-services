package org.zenframework.easyservices.test.descriptor;

import java.util.Collection;
import java.util.List;

import org.zenframework.easyservices.annotations.Value;

public interface CollectionUtil {

    String concat(@Value(typeParameters = { SimpleBean.class }) Collection<SimpleBean> list, String separator);

    @Value(typeParameters = { SimpleBean.class })
    List<SimpleBean> sort(@Value(typeParameters = { SimpleBean.class }) List<SimpleBean> list);

}
