package org.zenframework.easyservices.test.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.zenframework.easyservices.util.string.StringUtil;

public class CollectionUtilImpl implements CollectionUtil {

    @Override
    public String concat(final Collection<SimpleBean> list, String separator) {
        return StringUtil.concat(CollectionUtils.collect(list, new Transformer<Object, String>() {

            @Override
            public String transform(Object input) {
                return input.toString();
            }

        }, new ArrayList<String>(list.size())), separator);
    }

    @Override
    public List<SimpleBean> sortCopy(List<SimpleBean> str) {
        List<SimpleBean> list = new ArrayList<SimpleBean>(str);
        Collections.sort(list, new Comparator<SimpleBean>() {

            @Override
            public int compare(SimpleBean o1, SimpleBean o2) {
                return o1 == o2 ? 0 : o1 == null ? -1 : o2 == null ? 1 : Integer.compare(o1.getValue(), o2.getValue());
            }

        });
        return list;
    }

    @Override
    public void sortBeans(List<SimpleBean> list) {
        Collections.sort(list, new Comparator<SimpleBean>() {

            @Override
            public int compare(SimpleBean o1, SimpleBean o2) {
                return o1 == o2 ? 0 : o1 == null ? -1 : o2 == null ? 1 : Integer.compare(o1.getValue(), o2.getValue());
            }

        });
    }

    @Override
    public void sortInts(int[] values) {
        Arrays.sort(values);
    }

    @Override
    public void clearBean(SimpleBean bean) {
        bean.setName("");
        bean.setValue(0);
    }

}
