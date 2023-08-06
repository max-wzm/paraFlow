package org.paraflow.task;

import org.junit.Test;
import org.paraflow.action.TaskCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TaskTest {
    /**
     * sqrt(a^2, b^2)
     * a  \
     * --> c
     * b  /
     */
    @Test
    public void testEx() throws ExecutionException, InterruptedException {
        Task<Integer, Integer> a = new Task<>("a");
        a.setJob(1, (i) -> {
            try {
                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return i * i;
        });
        Task<Integer, Integer> b = new Task<>("b");
        b.setJob(2, (i) -> i * i);
        Task<Integer, Double> c = new Task<>("c");
        c.setJob(3, i -> {
            System.out.println("cdone");
            Integer ares = a.getResultWrapper().getResult();
            Integer bres = b.getResultWrapper().getResult();
            return i + Math.sqrt(ares + bres);
        });
        a.addNext(c);
        b.addNext(c);
        TaskFlow taskFlow = new TaskFlow();
        taskFlow.registerTasks(a, b, c);
        taskFlow.start();
        System.out.println(c.getResultWrapper().getResult());
    }

    @Test
    public void testComplx() throws ExecutionException, InterruptedException {
        Task<String, String> a = new Task<>();
        a.setJob(i -> {
            TaskFlow.sleepIgnoreInterrupt(1, TimeUnit.SECONDS);
            System.out.println("a throws");
            int k = 9 / 0;
            return "task_A";
        });
        a.setCallback(new TaskCallback<String>() {
            @Override
            public String onSuccess(String result) {
                System.out.println("hahah");
                return result+"-success-";
            }

            @Override
            public String onFailure(String result, Throwable exception) {
                System.out.println("Exception");
                exception.printStackTrace();
                return result+"-failure-";
            }
        });
        Task<String, String> b = new Task<>();
        b.setJob(i -> {
            System.out.println("task_b gets "+a.getTaskResult());
            TaskFlow.sleepIgnoreInterrupt(2, TimeUnit.SECONDS);
            return "task_b";
        });
        Task<String, String> c = new Task<>();
        c.setJob(i -> {
            TaskFlow.sleepIgnoreInterrupt(500, TimeUnit.MILLISECONDS);
            return "task_c";
        });
        Task<String, String> d = new Task<>();
        d.setJob(i -> {
            System.out.println("d gets "+b.getTaskResult() + c.getTaskResult());
            TaskFlow.sleepIgnoreInterrupt(1, TimeUnit.SECONDS);
            return "task_d";
        });
        Task<String, String> e = new Task<>();
        e.setJob(i -> {
            System.out.println("e gets "+b.getTaskResult()+d.getTaskResult());
            TaskFlow.sleepIgnoreInterrupt(1, TimeUnit.SECONDS);
            return "task_e";
        });
        a.addNext(b);
        b.addNext(e, d);
        c.addNext(b, d);
        d.addNext(e);
        /**
         *             / e1
         * a1 -> b2       |
         *   c.5 /___ \ d1
         */
        TaskFlow taskFlow = new TaskFlow();
        taskFlow.registerTasks(a, c);
        long oldtime = System.currentTimeMillis();
        taskFlow.start();
        System.out.println("all jobs done");
        System.out.println(System.currentTimeMillis() - oldtime);
    }

    @Test
    public void interruptStream() {
        try {
            IntStream.range(1, 10).forEach(i -> {
                System.out.println("int" + i);
                if (i == 6) {
                    throw new RuntimeException();
                }
            });
        } catch (Exception ignored) {
        }

    }
}