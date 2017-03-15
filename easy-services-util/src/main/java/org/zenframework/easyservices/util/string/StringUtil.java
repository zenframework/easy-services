package org.zenframework.easyservices.util.string;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StringUtil {

    private static final char[] ESCAPE = { '\t', '\b', '\n', '\r', '\f', '\'', '\"', '\\' };
    private static final char[] ESCAPED = { 't', 'b', 'n', 'r', 'f', '\'', '\"', '\\' };

    private static final String[] MONTH_NAMES = { "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь",
            "Ноябрь", "Декабрь" };

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
     * Метод разрезает строчку на куски установленной длины
     *
     * @param source      исходная строка
     * @param pieceLength длина фрагмента, на которые будет разбита исходная строка
     * @return массив, который получился в результате разбиения строки. Стоит иметь в виду, что если длина строки не
     *         делится без остатка на длину фрагмента, то последний фрагмент будет иметь длину, меньшую,
     *         чем заданая длина фрагмента
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
     * Приведение даты к требуемому формату
     *
     * @param date   форматируемая дата
     * @param format формат даты
     * @return строка, содержащая отформатированную дату
     */
    public static String dateToString(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * Приведение даты к требуемому формату
     *
     * @param date   строка, которую требуется привести к дате
     * @param format формат даты
     * @return дата, соответствующая строке
     * @throws ParseException ошибка, возникающая в случае
     *                        некорректного укащзания формата даты
     */
    public static Date stringToDate(String date, String format) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(date);
    }

    public static int getYearFromDate(Date date) {
        return Integer.valueOf(dateToString(date, "yyyy"));
    }

    public static int getMonthFromDate(Date date) {
        return Integer.valueOf(dateToString(date, "MM"));
    }

    public static int getDayFromDate(Date date) {
        return Integer.valueOf(dateToString(date, "dd"));
    }

    public static int getHourFromDate(Date date) {
        return Integer.valueOf(dateToString(date, "HH"));
    }

    public static int getMinFromDate(Date date) {
        return Integer.valueOf(dateToString(date, "mm"));
    }

    public static int getSecFromDate(Date date) {
        return Integer.valueOf(dateToString(date, "ss"));
    }

    public static String getMonthName(int i) {
        if (i < 1 || i > 12) {
            throw new IllegalArgumentException("Incorrect month: " + i);
        } else {
            return MONTH_NAMES[i - 1];
        }
    }

    /**
     * Передаваемая в качестве аргумента строка урезается до необходимой длины,
     * или заполняется до необходимой длины символами
     *
     * @param t     строка
     * @param len   итоговая длина строки
     * @param a     символ, которым заполняются недостающие позиции
     * @param align с какой строны будут заполняться лишние символы
     * @return отформатированная строка
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
     * Округление числа до требуемого количества знаков после запятой
     *
     * @param d  округляемое знгачение
     * @param dz количество знаков после запятой
     * @return отформатированную переменную
     */
    public static Double formatDouble(Double d, int dz) {
        Double res = null;
        if (d != null)
            res = new BigDecimal(d).setScale(dz, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        return res;
    }

    /**
     * Этот ментод разбивает коллекцию по строкам и выводит в качестве голого
     * текста
     *
     * @param collection разбиваемая коллеция
     * @return текст, в котором коллекция разбита построчно
     */
    public static String splitCollection(Iterable<?> collection) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection)
            sb.insert(sb.length(), o + "\n");
        return sb.toString();
    }

    public static String splitCollection(Object[] collection, String splitter) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection)
            sb.append(sb.length() == 0 ? "" : splitter).append(o);
        return sb.toString();
    }

    public static String splitCollection(Iterable<?> collection, String splitter) {
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

    public static String htmlspecialchar(CharSequence content) {
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

    /**
     * Метод транслитерирует русский текст
     *
     * @param text текст на русском языке
     * @return транслитерированный текст
     */
    public static String translit(String text) {
        StringBuilder res = new StringBuilder();
        if (text != null) {
            for (char ch : text.toCharArray())
                res.append(translit(ch));
        }
        return res.toString();
    }

    /**
     * Метод транслитерирует русский символ
     *
     * @param ch символ на русском языке
     * @return транслитерированный текст
     */
    public static String translit(char ch) {
        // ГОСТ 16876-71
        switch (ch) {
        case 'А':
            return "A";
        case 'Б':
            return "B";
        case 'В':
            return "V";
        case 'Г':
            return "G";
        case 'Д':
            return "D";
        case 'Е':
            return "E";
        case 'Ё':
            return "JO";
        case 'Ж':
            return "ZH";
        case 'З':
            return "Z";
        case 'И':
            return "I";
        case 'Й':
            return "JJ";
        case 'К':
            return "K";
        case 'Л':
            return "L";
        case 'М':
            return "M";
        case 'Н':
            return "N";
        case 'О':
            return "O";
        case 'П':
            return "P";
        case 'Р':
            return "R";
        case 'С':
            return "S";
        case 'Т':
            return "T";
        case 'У':
            return "U";
        case 'Ф':
            return "F";
        case 'Х':
            return "KH";
        case 'Ц':
            return "C";
        case 'Ч':
            return "CH";
        case 'Ш':
            return "SH";
        case 'Щ':
            return "SHH";
        case 'Ъ':
            return "\"";
        case 'Ы':
            return "Y";
        case 'Ь':
            return "'";
        case 'Э':
            return "EH";
        case 'Ю':
            return "JU";
        case 'Я':
            return "JA";
        case '`':
            return "*";
        case 'а':
            return "a";
        case 'б':
            return "b";
        case 'в':
            return "v";
        case 'г':
            return "g";
        case 'д':
            return "d";
        case 'е':
            return "e";
        case 'ё':
            return "yo";
        case 'ж':
            return "zh";
        case 'з':
            return "z";
        case 'и':
            return "i";
        case 'й':
            return "jj";
        case 'к':
            return "k";
        case 'л':
            return "l";
        case 'м':
            return "m";
        case 'н':
            return "n";
        case 'о':
            return "o";
        case 'п':
            return "p";
        case 'р':
            return "r";
        case 'с':
            return "s";
        case 'т':
            return "t";
        case 'у':
            return "u";
        case 'ф':
            return "f";
        case 'х':
            return "kh";
        case 'ц':
            return "c";
        case 'ч':
            return "ch";
        case 'ш':
            return "sh";
        case 'щ':
            return "shh";
        case 'ъ':
            return "\"";
        case 'ы':
            return "y";
        case 'ь':
            return "'";
        case 'э':
            return "eh";
        case 'ю':
            return "ju";
        case 'я':
            return "ja";
        default:
            return String.valueOf(ch);
        }
    }

    public static String getNumberName(Number number, String name, String a, String b, String c) {

        // пример использования:

        //    public static void main(String... args) {
        //        for (int i = -100; i < 1100; ++i) {
        //            System.out.println(i + " " + getNumberName(i, "файл", "", "а", "ов"));
        //        }
        //    }

        int rest = Math.abs(number.intValue()) % 100;
        if (rest == 1)
            return name + a;
        if (rest > 1 && rest < 5)
            return name + b;
        if (rest == 0 || (rest >= 5 && rest < 21))
            return name + c;
        return getNumberName(rest % 10, name, a, b, c);
    }

    /**
     * Возвращает строку с подставлеными значениями аргументов
     * вместо параметров вида {n} (номера параметров начинаются с 0).
     * Параметр {all} заменяется на список всех значений аргументов.
     *
     * @param s    Строка с параметрами
     * @param args Аргументы
     * @return Строка с подставленными значениями
     */
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

}
