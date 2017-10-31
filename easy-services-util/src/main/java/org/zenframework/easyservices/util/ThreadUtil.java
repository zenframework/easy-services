package org.zenframework.easyservices.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.zenframework.easyservices.util.thread.MultiCauseException;
import org.zenframework.easyservices.util.thread.Task;

public class ThreadUtil {

    private ThreadUtil() {}

    public static void runMultiThreadTask(final Task task, int threads, final String threadNamePrefix) throws MultiCauseException, InterruptedException {
        runMultiThreadTask(task, threads, new ThreadFactory() {

            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, threadNamePrefix + '-' + counter.getAndIncrement());
            }

        });
    }

    public static void runMultiThreadTask(final Task task, int threads, ThreadFactory threadFactory) throws MultiCauseException, InterruptedException {
        Thread[] workers = new Thread[threads];
        final Collection<Throwable> errors = Collections.synchronizedCollection(new LinkedList<Throwable>());
        for (int i = 0; i < threads; i++) {
            final int n = i;
            workers[i] = threadFactory.newThread(new java.lang.Runnable() {

                @Override
                public void run() {
                    try {
                        task.run(n);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        errors.add(e);
                    }
                }

            });
            workers[i].start();
        }
        for (int i = 0; i < threads; i++)
            workers[i].join();
        if (!errors.isEmpty())
            throw new MultiCauseException("Some threads have failed", errors);
    }

}
