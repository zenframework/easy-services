package org.zenframework.easyservices.util.debug;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class TimeStat {

    private static final Map<String, Map<String, Statistics>> STATISTICS = new HashMap<String, Map<String, Statistics>>();
    private static final ThreadLocal<TimeStat> TIME_STATS = new ThreadLocal<TimeStat>();

    private final String name;
    private final Map<String, Statistics> stages;
    private long time = System.nanoTime();

    private TimeStat(String name, Map<String, Statistics> stages) {
        this.name = name;
        this.stages = stages;
    }

    public void stageFinished(String stage) {
        Statistics stageStat;
        synchronized (stages) {
            stageStat = stages.get(stage);
            if (stageStat == null) {
                stageStat = new Statistics();
                stages.put(stage, stageStat);
            }
        }
        long finished = System.nanoTime();
        double diff = ((double) (finished - time)) / 1000000;
        stageStat.addValue(diff);
        time = finished;
    }

    @Override
    public String toString() {
        return toString(10, 10, 10, 10);
    }

    public String toString(int countFieldWidth, int minFieldWidth, int maxFieldWidth, int avgFieldWidth) {
        StringBuilder str = new StringBuilder().append(name).append(" time statistics:");
        int stageNameLen = 0;
        for (String name : stages.keySet())
            stageNameLen = Math.max(stageNameLen, name.length());
        if (stageNameLen > 0) {
            String headerFormat = "%-" + (stageNameLen + 4) + "s|%" + countFieldWidth + "s|%" + (minFieldWidth + 2) + "s|%" + (maxFieldWidth + 2)
                    + "s|%" + (avgFieldWidth + 2) + "s";
            str.append("\n\t").append(String.format(headerFormat, "Stage", "Count", "Min", "Max", "Avg"));
            String rowFormat = "%-" + (stageNameLen + 4) + "s|%" + countFieldWidth + "d|%" + minFieldWidth + ".2fms|%" + maxFieldWidth + ".2fms|%"
                    + avgFieldWidth + ".2fms";
            str.append("\n\t").append(StringUtils.leftPad("", stageNameLen + 4, '-')).append('+')
                    .append(StringUtils.leftPad("", countFieldWidth, '-')).append('+').append(StringUtils.leftPad("", minFieldWidth + 2, '-'))
                    .append('+').append(StringUtils.leftPad("", maxFieldWidth + 2, '-')).append('+')
                    .append(StringUtils.leftPad("", avgFieldWidth + 2, '-'));
            for (Map.Entry<String, Statistics> entry : stages.entrySet()) {
                String name = entry.getKey();
                Statistics stat = entry.getValue();
                str.append("\n\t").append(String.format(rowFormat, name, stat.getCount(), stat.getMin(), stat.getMax(), stat.getAvg()));
            }
        }
        return str.toString();
    }

    public static void setThreadTimeStat(Class<?> cls, String methodName) {
        setThreadTimeStat(getName(cls, methodName));
    }

    public static void setThreadTimeStat(String name) {
        TimeStat timeStat = TIME_STATS.get();
        if (timeStat == null) {
            timeStat = new TimeStat(name, getStages(name));
            TIME_STATS.set(timeStat);
        }
    }

    public static TimeStat getThreadTimeStat() {
        return TIME_STATS.get();
    }

    public static void removeThreadTimeStat() {
        TIME_STATS.remove();
    }

    public static TimeStat getTimeStat(Class<?> cls, String methodName) {
        return getTimeStat(getName(cls, methodName));
    }

    public static TimeStat getTimeStat(String name) {
        return new TimeStat(name, getStages(name));
    }

    public static void clearTimeStat(Class<?> cls, String methodName) {
        clearTimeStat(getName(cls, methodName));
    }

    public static void clearTimeStat(String name) {
        getStages(name).clear();
    }

    private static String getName(Class<?> cls, String methodName) {
        return cls.getSimpleName() + '.' + methodName + "()";
    }

    private static Map<String, Statistics> getStages(String name) {
        synchronized (STATISTICS) {
            Map<String, Statistics> stages = STATISTICS.get(name);
            if (stages == null) {
                stages = new LinkedHashMap<String, Statistics>();
                STATISTICS.put(name, stages);
            }
            return stages;
        }
    }

}
