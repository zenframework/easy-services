package org.zenframework.easyservices.util.debug;

import org.slf4j.Logger;
import org.zenframework.easyservices.util.string.ParameterizedString;

import java.text.SimpleDateFormat;

public class TimeChecker {

    @SuppressWarnings("unused")
    private static final String PROPERTY_THRESHOLD = "timeChecker.threshold";

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private final long startTime = currentTime();
    private final ParameterizedString taskName;
    private final String prefix;

    private Logger logger;

    public static boolean SHOW_STACK_DEPTH = false;

    public TimeChecker(String taskName, Logger logger) {
        this(taskName, logger, "");
    }

    public TimeChecker(ParameterizedString taskName, Logger logger) {
        this(taskName, logger, "");
    }

    public TimeChecker(String taskName, Logger logger, String prefix) {
        this(new ParameterizedString(taskName), logger, prefix);
    }

    public TimeChecker(ParameterizedString taskName, Logger logger, String prefix, Object... args) {
        this.taskName = taskName;
        this.logger = logger;
        this.prefix = prefix;
        showLog("[" + this.taskName + "] Started at " + new SimpleDateFormat().format(startTime));
    }

    public long getDifference() {
        return currentTime() - startTime;
    }

    public long getUsedMemory() {
        return (RUNTIME.totalMemory() - RUNTIME.freeMemory()) / 0x100000;
    }

    public void printDifference() {
        showLog("[" + taskName + "] COMPLETED - time elapsed: " + getDifference() + "ms, memory: " + getUsedMemory() + "MB");
    }

    public void printDifference(Object result) {
        showLog("[" + taskName + "] COMPLETED [RESULT: " + result + "] - time elapsed: " + getDifference() + "ms, memory: "
                + getUsedMemory() + "MB");
    }

    public void print(Object print) {
        showLog("[" + taskName + "] " + print);
    }

    private void showLog(String log) {
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(getPrefix() + log);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String getPrefix() {
        //        Thread thread = Thread.currentThread();
        //        Long id = thread.getId();
        //        String threadName = '[' + thread.getName() + "] ";

        Integer depth = getStackDepth();
        StringBuilder res = new StringBuilder(prefix);
        for (int i = 0; i < (depth == null ? 0 : depth); ++i) {
            res.append("> ");
        }
        return res.toString();
    }

    private static long currentTime() {
        return System.currentTimeMillis();
    }

    private int getStackDepth() {

        int res = 0;

        if (SHOW_STACK_DEPTH) {
            try {
                throw new Exception();
            } catch (Throwable e) {
                for (StackTraceElement element : e.getStackTrace()) {
                    if (!element.getClassName().equals(TimeChecker.class.getName())) {
                        res++;
                    }
                }
            }
        }

        return res;
    }

}
