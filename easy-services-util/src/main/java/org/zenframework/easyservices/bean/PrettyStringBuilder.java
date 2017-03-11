package org.zenframework.easyservices.bean;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Constructs pretty string representation of object value.
 */
public class PrettyStringBuilder {

    private static final String PROP_DEBUG_PRETTY_BEANS = "debug.prettyBeans";

    protected int maxArrayLen = 10;
    protected int maxDeep = 3;
    protected int deep;
    protected String nullValue = "<null>";
    protected int tab = 0;

    public PrettyStringBuilder() {}

    public int getMaxArrayLen() {
        return maxArrayLen;
    }

    public void setMaxArrayLen(int maxArrayLen) {
        this.maxArrayLen = maxArrayLen;
    }

    public int getMaxDeep() {
        return maxDeep;
    }

    public void setMaxDeep(int maxDeep) {
        this.maxDeep = maxDeep;
    }

    public String getNullValue() {
        return nullValue;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * Returns pretty value from object value.
     */
    protected void toPrettyString(StringBuilder s, Object obj) {
        deep++;
        if (obj == null) {
            s.append(nullValue);
        } else if (deep == maxDeep) {
            s.append(beanToString(obj));
        } else {
            Class<?> c = obj.getClass();
            if (c.isArray()) {
                int arrayLen = Array.getLength(obj);
                int len = Math.min(arrayLen, maxArrayLen);
                s.append('[');
                tab++;
                for (int i = 0; i < len; i++) {
                    newLine(s);
                    toPrettyString(s, Array.get(obj, i));
                    s.append(", ");
                }
                if (len < arrayLen) {
                    newLine(s);
                    s.append("...  ");
                }
                if (s.length() > 2 + tab)
                    s.setLength(s.length() - 2);
                tab--;
                if (Array.getLength(obj) > 0)
                    newLine(s);
                s.append(']');
            } else if (obj instanceof Collection) {
                Collection<?> coll = (Collection<?>) obj;
                Iterator<?> it = coll.iterator();
                int i;
                s.append('(');
                tab++;
                newLine(s);
                for (i = 0; it.hasNext() && i < maxArrayLen; i++) {
                    toPrettyString(s, it.next());
                    s.append(", ");
                    newLine(s);
                }
                if (i < coll.size()) {
                    s.append("...  ");
                    newLine(s);
                }
                if (s.length() > 2 + tab) {
                    s.setLength(s.length() - 3 - tab);
                }
                tab--;
                newLine(s);
                s.append(')');
            } else if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                Iterator<?> it = map.keySet().iterator();
                int i = 0;
                s.append('{');
                tab++;
                newLine(s);
                while ((it.hasNext() && (i < maxArrayLen))) {
                    Object key = it.next();
                    s.append(key).append(':');
                    toPrettyString(s, map.get(key));
                    s.append(", ");
                    newLine(s);
                    i++;
                }
                if (i < map.size()) {
                    s.append("...  ");
                }
                if (s.length() > 2 + tab) {
                    s.setLength(s.length() - 3 - tab);
                }
                tab--;
                newLine(s);
                s.append('}');
            } else {
                s.append(beanToString(obj));
            }
        }
        deep--;
    }

    /**
     * Returns pretty string representation of the object.
     */
    public String toString(Object value) {
        StringBuilder s = new StringBuilder(1024);
        toPrettyString(s, value);
        return s.toString();
    }

    private String beanToString(Object obj) {
        if (System.getProperty(PROP_DEBUG_PRETTY_BEANS) != null) {
            return ToStringBuilder.reflectionToString(obj, ToStringStyle.MULTI_LINE_STYLE);
        } else {
            return obj.toString();
        }
    }

    private void newLine(StringBuilder s) {
        s.append('\n');
        for (int i = 0; i < tab; i++) {
            s.append('\t');
        }
    }

}