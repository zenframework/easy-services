package org.zenframework.easyservices.test;

import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

public class TestUtil {

    private TestUtil() {}

    public static void runMultiThreads(final Runnable runnable, int threads) throws InterruptedException {
        Thread[] workers = new Thread[threads];
        final AtomicBoolean failed = new AtomicBoolean(false);
        for (int i = 0; i < threads; i++) {
            final int n = i;
            workers[i] = new Thread(new java.lang.Runnable() {

                @Override
                public void run() {
                    try {
                        runnable.run(n);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        failed.set(true);
                    }
                }

            }, "TestWorker-" + i);
            workers[i].start();
        }
        for (int i = 0; i < threads; i++)
            workers[i].join();
        if (failed.get())
            TestCase.fail("Some error has happened");
    }

    public static interface Runnable {

        void run(int n) throws Exception;

    }

}
