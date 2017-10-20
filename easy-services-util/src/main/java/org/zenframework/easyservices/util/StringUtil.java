package org.zenframework.easyservices.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

public class StringUtil {

    private static final char[] ESCAPE = { '\t', '\b', '\n', '\r', '\f', '\'', '\"', '\\' };
    private static final char[] ESCAPED = { 't', 'b', 'n', 'r', 'f', '\'', '\"', '\\' };

    public static final int ALIGN_RIGHT = 0;
    public static final int ALIGN_LEFT = 1;

    private StringUtil() {/**/}

    public static StringBuilder indent(StringBuilder str, int indent, boolean newLine) {
        if (newLine)
            str.append('\n');
        for (int i = 0; i < indent; i++)
            str.append('\t');
        return str;
    }

    public static String escape(String s) {
        StringBuilder str = new StringBuilder(s.length() * 2);
        for (char c : s.toCharArray()) {
            char escaped = escaped(c);
            if (escaped != 0)
                str.append('\\').append(escaped);
            else
                str.append(c);
        }
        return str.toString();
    }

    public static String concat(Iterable<String> strs, String separator) {
        StringBuilder str = new StringBuilder();
        for (String s : strs)
            str.append(s).append(separator);
        if (str.length() > 0)
            str.setLength(str.length() - separator.length());
        return str.toString();
    }

    private static char escaped(char c) {
        for (int i = 0; i < ESCAPE.length; i++)
            if (c == ESCAPE[i])
                return ESCAPED[i];
        return 0;
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || "".equals(string);
    }

    public static String removeTags(String text) {
        return text.replaceAll("[\\p{Space}]+<", "<").replaceAll(">[\\p{Space}]+", ">").replaceAll(">[\\p{Space}]+<", "><").replaceAll("<.*?>", " ")
                .replaceAll(" ,", ",").trim().replaceAll("[ \t]+", " ");
    }

    public static String removeTagsWithBrokenOnes(String text) {
        text = removeTags(text);
        if (text.contains("<"))
            text = getTextBeforeWord(text, "<").trim();
        return text;
    }

    public static String getTextBetweenWords(String source, String begin, String end) {
        String res = null;
        if (source != null && begin != null && end != null) {
            int firstLetterIndex = source.indexOf(begin);
            if (firstLetterIndex >= 0) {
                firstLetterIndex += begin.length();
                int lastLetterIndex = source.indexOf(end, firstLetterIndex);
                if (lastLetterIndex >= 0 && firstLetterIndex <= lastLetterIndex)
                    res = source.substring(firstLetterIndex, lastLetterIndex);
            }
        }
        return res;
    }

    public static String getTextBetweenWordsFromTheEnd(String source, String begin, String end) {
        String res = null;
        if (source != null && begin != null && end != null) {
            int ii = source.indexOf(end);
            int i = -1;
            int tt;
            while ((tt = source.indexOf(begin, i + 1)) < ii && tt >= 0)
                i = tt;
            if (i >= 0) {
                i += begin.length();
                if (ii >= 0 && i <= ii)
                    res = source.substring(i, ii);
            }
        }
        return res;
    }

    public static List<String> getTextListWithWordsBetweenWords(String source, String begin, String end) {
        List<String> res = new LinkedList<String>();
        while ((source = getTextAfterWord(source, begin)) != null) {
            String tmp = getTextBeforeWord(source, end);
            if (tmp != null)
                res.add(tmp);
        }
        return res;
    }

    public static String getTextAfterWord(String source, String word) {
        String res = null;
        if (source != null && word != null) {
            int i = source.indexOf(word);
            if (i >= 0) {
                i += word.length();
                res = source.substring(i, source.length());
            }
        }
        return res;
    }

    public static String getTextBeforeWord(String source, String word) {
        String res = null;
        if (source != null && word != null) {
            int i = source.indexOf(word);
            if (i >= 0)
                res = source.substring(0, i);
        }
        return res;
    }

    /**
     * Cuts a string to pieces of a certain length
     *
     * @param source string to be cut
     * @param pieceLength piece length
     * @return array of string pieces
     */
    public static String[] cut(String source, int pieceLength) {
        int sourceLength = source.length();
        int count = (int) Math.ceil((double) sourceLength / (double) pieceLength);

        String[] res = new String[count];

        for (int i = 0; i < count; ++i) {
            int start = i * pieceLength;
            int end = start + pieceLength;
            if (end > sourceLength)
                end = sourceLength;
            String piece = source.substring(start, end);
            res[i] = piece;
        }

        return res;
    }

    /**
     * Cuts or spreads a string to a fixed length
     *
     * @param t     string
     * @param len   new length
     * @param a     char to fill sorter string with
     * @param align align
     * @return formatted string
     */
    public static String toFixLength(String t, int len, char a, int align) {
        int size = t.length();
        if (size >= len) {
            t = t.substring(0, len);
        } else {
            for (int i = 0; i < len - size; i++) {
                if (align == ALIGN_LEFT)
                    t += a;
                else if (align == ALIGN_RIGHT)
                    t = a + t;
            }
        }
        return t;
    }

    /**
     * Rounds a number to a certain preceision
     *
     * @param d  number
     * @param dz number of digits after point
     * @return formatted number
     */
    public static Double formatDouble(Double d, int dz) {
        Double res = null;
        if (d != null)
            res = new BigDecimal(d).setScale(dz, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        return res;
    }

    public static String toString(Iterable<?> collection) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection)
            sb.append(o).append('\n');
        return sb.toString();
    }

    public static String toString(Object[] collection, String splitter) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection)
            sb.append(sb.length() == 0 ? "" : splitter).append(o);
        return sb.toString();
    }

    public static String toString(Iterable<?> collection, String splitter) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection)
            sb.append(sb.length() == 0 ? "" : splitter).append(o);
        return sb.toString();
    }

    public static String quote(Object aObject) {
        return '\'' + String.valueOf(aObject) + '\'';
    }

    public static Collection<Integer> stringToIntegerCollection(String text) {
        Collection<Integer> res = new LinkedList<Integer>();
        for (String t : trim(text).split("[\\p{Space}\\p{Punct}]+")) {
            if (!"".equals(t = trim(t)))
                res.add(Integer.valueOf(t));
        }
        return res;
    }

    public static String top(String text, int numberOfLetters) {
        return text.substring(0, numberOfLetters);
    }

    public static String trim(String text) {
        return text != null ? text.trim() : "";
    }

    public static String xmlEncode(CharSequence content) {
        StringBuilder sb = new StringBuilder();
        if (content != null) {
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String switchFirstLetterCase(String s, boolean up) {
        if (isNullOrEmpty(s))
            return s;
        char c[] = s.toCharArray();
        if (up && c[0] >= 'a' && c[0] <= 'z')
            c[0] = (char) (c[0] - 'a' + 'A');
        else if (!up && c[0] >= 'A' && c[0] <= 'Z')
            c[0] = (char) (c[0] - 'A' + 'a');
        return new String(c);
    }

    public static String getSimpleClassNameFromCanonicalName(String className) {
        if (isNullOrEmpty(className))
            return className;
        int pos = className.lastIndexOf(".");
        if (pos < 0)
            return className;
        else
            return className.substring(pos + 1);
    }

    public static String getStringWithArgs(String s, Object... args) {
        if (args == null)
            args = new Object[0];
        StringBuilder str = new StringBuilder(s);
        for (int i = 0; i < args.length; i++)
            findReplaceArg(str, "{" + i + '}', args[i]);
        findReplaceArg(str, "{all}", args);
        return str.toString();
    }

    private static void findReplaceArg(StringBuilder str, String arg, Object value) {
        int j = str.indexOf(arg);
        if (j >= 0) {
            String replace = value == null ? "null" : value instanceof Object[] ? Arrays.deepToString((Object[]) value) : value.toString();
            for (; j >= 0; j = str.indexOf(arg, j))
                str.replace(j, j + arg.length(), replace);
        }
    }

    public static String getNullEndingString(byte[] bytes, Charset charset) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0)
                return new String(bytes, 0, i, charset);
        }
        return new String(bytes, charset);
    }

    public static Map<String, String> toMap(String header, String keyValueSeparator, String pairsSeparator) {
        Map<String, String> map = new HashMap<String, String>();
        String[] pairs = header == null ? new String[0] : header.split(pairsSeparator);
        for (String pair : pairs) {
            int idx = pair.indexOf(keyValueSeparator);
            String key = idx > 0 ? pair.substring(0, idx).trim() : pair.trim();
            String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1).trim() : null;
            map.put(key, value);
        }
        return map;
    }

    public static String deepToString(Object obj) {
        StringWriter w = new StringWriter();
        try {
            deepToString(w, obj);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return w.toString();
    }

    public static void deepToString(Writer writer, Object obj) throws IOException {
        if (obj.getClass().isArray()) {
            writer.write('[');
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (i > 0)
                    writer.write(", ");
                deepToString(writer, Array.get(obj, i));
            }
            writer.write(']');
        } else if (obj instanceof Iterable) {
            writer.write('[');
            boolean tail = false;
            for (Object o : (Iterable<?>) obj) {
                if (tail)
                    writer.write(", ");
                deepToString(writer, o);
                tail = true;
            }
            writer.write(']');
        } else {
            writer.write(obj.toString());
        }
    }

}
