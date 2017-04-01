package org.zenframework.easyservices.util.debug;

public class Statistics {

    private int count = 0;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private double avg = 0;

    public synchronized void addValue(double value) {
        if (value < min)
            min = value;
        if (value > max)
            max = value;
        avg = (avg * count + value) / (count + 1);
        count++;
    }

    public synchronized void mergeWith(Statistics s) {
        if (s.getMin() < min)
            min = s.getMin();
        if (s.getMax() > max)
            max = s.getMax();
        avg = (avg * count + s.getAvg() * s.getCount()) / (count + s.getCount());
        count += s.getCount();
    }

    public int getCount() {
        return count;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getAvg() {
        return avg;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("count: ").append(count).append(", min: ").append(min).append(", max: ").append(max).append(", avg: ")
                .append(avg).toString();
    }

}
