package org.zenframework.easyservices.test.descriptor;

import java.util.Collection;
import java.util.List;

import org.zenframework.easyservices.annotations.TypeParameters;

public interface CollectionUtil {

    String concat(@TypeParameters({ SimpleBean.class }) Collection<SimpleBean> list, String separator);

    @TypeParameters({ SimpleBean.class })
    List<SimpleBean> sort(@TypeParameters({ SimpleBean.class }) List<SimpleBean> list);

}
