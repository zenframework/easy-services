package org.zenframework.easyservices.test.descriptor;

import java.util.Collection;
import java.util.List;

import org.zenframework.easyservices.annotations.Out;
import org.zenframework.easyservices.annotations.TypeParameters;

public interface CollectionUtil {

    String concat(@TypeParameters({ SimpleBean.class }) Collection<SimpleBean> list, String separator);

    @TypeParameters({ SimpleBean.class })
    List<SimpleBean> sortCopy(@TypeParameters({ SimpleBean.class }) List<SimpleBean> list);

    void sortBeans(@Out @TypeParameters({ SimpleBean.class }) List<SimpleBean> list);

    void sortInts(@Out int[] values);

    void clearBean(@Out SimpleBean bean);

}
