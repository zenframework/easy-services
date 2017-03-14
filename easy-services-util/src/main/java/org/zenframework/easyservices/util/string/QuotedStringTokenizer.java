package org.zenframework.easyservices.util.string;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuotedStringTokenizer implements Enumeration<String> {

    private static final Pattern PATTERN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    private final Matcher matcher;
    private Boolean found = null;

    public QuotedStringTokenizer(String str) {
        matcher = PATTERN.matcher(str);
    }

    @Override
    public boolean hasMoreElements() {
        return find();
    }

    @Override
    public String nextElement() {
        if (!find())
            throw new NoSuchElementException();
        String result;
        if (matcher.group(1) != null)
            result = matcher.group(1);
        else
            result = matcher.group(2);
        found = null;
        return result;
    }

    private boolean find() {
        if (found == null)
            found = matcher.find();
        return found;
    }

}
