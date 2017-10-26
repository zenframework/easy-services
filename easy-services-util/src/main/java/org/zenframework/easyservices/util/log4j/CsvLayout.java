package org.zenframework.easyservices.util.log4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class CsvLayout extends Layout {

    private List<String> loggers = new ArrayList<>(100);

    private int maxLoggers = 100;
    private char delimeter = ';';
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void activateOptions() {}

    @Override
    public String getHeader() {
        StringBuilder str = new StringBuilder();
        synchronized (loggers) {
            for (int i = 0; i < loggers.size(); i++)
                str.append(loggers.get(i)).append(delimeter);
            str.append('\n');
        }
        return str.toString();
    }

    @Override
    public String getFooter() {
        return getHeader();
    }

    @Override
    public String format(LoggingEvent event) {
        StringBuilder str = new StringBuilder();
        int loggerPos = getLoggerPos(event.getLoggerName());
        str.append(quotes(dateFormat.format(new Date(event.getTimeStamp())))).append(delimeter).append(quotes(event.getThreadName()))
                .append(delimeter);
        for (int i = 0; i < loggerPos; i++)
            str.append(delimeter);
        str.append('"').append(escape(event.getRenderedMessage()));
        if (event.getThrowableInformation() != null)
            str.append(escape(toString(event.getThrowableInformation().getThrowable())));
        str.append("\"\n");
        return str.toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    public void setMaxLoggers(int maxLoggers) {
        this.maxLoggers = maxLoggers;
    }

    public void setDelimeter(char columnDelimeter) {
        this.delimeter = columnDelimeter;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }

    private int getLoggerPos(String loggerName) {
        synchronized (loggers) {
            int pos = loggers.indexOf(loggerName);
            if (pos < 0) {
                loggers.add(loggerName);
                pos = loggers.size() - 1;
            }
            return pos;
        }
    }

    private static String quotes(String message) {
        return '"' + message.replace("\"", "\"\"") + '"';
    }

    private static String escape(String message) {
        return message.replace("\"", "\"\"");
    }

    private static String toString(Throwable e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        return out.toString();
    }

}
