package org.zenframework.easyservices.js.babel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.debug.TimeChecker;

import junit.framework.TestCase;

public class PooledBabelTest extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(PooledBabelTest.class);

    public void testPool() throws Exception {
        PooledBabel babel = new PooledBabel(new DefaultBabelPool());
        List<Task> tasks = new ArrayList<Task>(100);
        for (int i = 0; i < 100; i++) {
            Task task = new Task(babel, i);
            tasks.add(task);
            new Thread(task).start();
        }
        List<Task> failed = new ArrayList<Task>(100);
        while (!tasks.isEmpty()) {
            Iterator<Task> it = tasks.iterator();
            while (it.hasNext()) {
                Task task = it.next();
                if (task.result != null) {
                    if (!task.result.equals("React.createElement(Hello, { n: " + task.n + " });"))
                        failed.add(task);
                    it.remove();
                }
            }
            Thread.sleep(100);
        }
        assertEquals(Collections.emptyList(), failed);
    }

    private static class Task implements Runnable {

        private final Babel babel;
        private final int n;
        private volatile String result = null;

        Task(Babel babel, int n) {
            this.babel = babel;
            this.n = n;
        }

        @Override
        public void run() {
            TimeChecker time = new TimeChecker("Evaluate script", LOG);
            try {
                String script = "<Hello n={" + n + "} />";
                result = babel.transform(script, Babel.PRESET_REACT);
                time.printDifference(result);
            } catch (Throwable e) {
                result = e.getMessage();
                time.printDifference(e);
            }
        }

        @Override
        public String toString() {
            return Integer.toString(n);
        }

    }

}
