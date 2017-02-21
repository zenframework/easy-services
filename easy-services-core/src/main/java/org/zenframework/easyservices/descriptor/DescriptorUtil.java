package org.zenframework.easyservices.descriptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.zenframework.easyservices.ValueTransfer;

public class DescriptorUtil {

    public static final Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {

        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            int w1 = o1.isInterface() ? 0 : 1;
            int w2 = o2.isInterface() ? 0 : 1;
            return o1.equals(o2) ? 0 : o1.isAssignableFrom(o2) ? -1 : o2.isAssignableFrom(o1) ? 1 : Integer.compare(w1, w2);
        }

    };

    private DescriptorUtil() {}

    @SuppressWarnings("unchecked")
    public static List<Class<?>> getAllAssignableFrom(Class<?> cls) {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(cls);
        set.addAll(ClassUtils.getAllInterfaces(cls));
        set.addAll(ClassUtils.getAllSuperclasses(cls));
        List<Class<?>> list = new ArrayList<Class<?>>(set);
        topologicalSort(list, CLASS_COMPARATOR);
        return list;
    }

    public static ClassDefaults merge(ClassDefaults oldValue, ClassDefaults newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            oldValue.setValueDescriptor(merge(oldValue.getValueDescriptor(), newValue.getValueDescriptor()));
            if (newValue.getDebug() != null)
                oldValue.setDebug(newValue.getDebug());
        }
        return oldValue;
    }

    public static MethodDescriptor merge(MethodDescriptor oldValue, MethodDescriptor newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            if (newValue.getAlias() != null)
                oldValue.setAlias(newValue.getAlias());
            if (newValue.getClose() != null)
                oldValue.setClose(newValue.getClose());
            oldValue.setReturnDescriptor(merge(oldValue.getReturnDescriptor(), newValue.getReturnDescriptor()));
            for (int i = 0; i < newValue.getParameterDescriptors().length; i++)
                oldValue.setParameterDescriptor(i, merge(oldValue.getParameterDescriptor(i), newValue.getParameterDescriptor(i)));
        }
        return oldValue;
    }

    public static ValueDescriptor merge(ValueDescriptor oldValue, ValueDescriptor newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            if (newValue.getTransfer() != null && newValue.getTransfer() != ValueTransfer.DEFAULT)
                oldValue.setTransfer(newValue.getTransfer());
            if (newValue.getTypeParameters() != null && newValue.getTypeParameters().length > 0)
                oldValue.setTypeParameters(newValue.getTypeParameters());
        }
        return oldValue;
    }

    public static ParamDescriptor merge(ParamDescriptor oldValue, ParamDescriptor newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            oldValue.setTransfer(newValue.getTransfer());
            oldValue.setTypeParameters(newValue.getTypeParameters());
            oldValue.setClose(newValue.isClose());
        }
        return oldValue;
    }

    private static <T> void topologicalSort(List<T> list, Comparator<T> comparator) {
        List<T> out = new ArrayList<T>(list.size());
        while (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                T a = list.get(i);
                boolean noIncomingEdges = true;
                for (int j = 0; j < list.size(); j++) {
                    if (i != j && comparator.compare(a, list.get(j)) > 0) {
                        noIncomingEdges = false;
                        break;
                    }
                }
                if (noIncomingEdges) {
                    out.add(list.remove(i));
                    break;
                }
            }
        }
        list.addAll(out);
    }

}
