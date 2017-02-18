package org.zenframework.easyservices.descriptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;

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

    public static ClassDescriptor merge(ClassDescriptor oldValue, ClassDescriptor newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            ValueDescriptor valueDescriptor = newValue.getValueDescriptor();
            if (valueDescriptor != null)
                oldValue.setValueDescriptor(valueDescriptor);
            Boolean debug = newValue.getDebug();
            if (debug != null)
                oldValue.setDebug(debug);
        }
        return oldValue;
    }

    public static MethodDescriptor merge(MethodDescriptor oldValue, MethodDescriptor newValue) {
        if (oldValue == null)
            return newValue;
        if (newValue != null) {
            String alias = newValue.getAlias();
            if (alias != null)
                oldValue.setAlias(alias);
            ValueDescriptor returnDescriptor = newValue.getReturnDescriptor();
            if (returnDescriptor != null)
                oldValue.setReturnDescriptor(returnDescriptor);
            for (int i = 0; i < newValue.getParameterDescriptors().length; i++) {
                ValueDescriptor paramDescriptor = newValue.getParameterDescriptors()[i];
                if (paramDescriptor != null)
                    oldValue.setParameterDescriptor(i, paramDescriptor);
            }
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
