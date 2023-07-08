package org.paraflow.task;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

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
            try {
                Thread.sleep(2000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        Task<String, String> b = new Task<>();
        b.setJob(i -> {
            try {
                Thread.sleep(3000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        Task<String, String> c = new Task<>();
        c.setJob(i -> {
            try {
                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        Task<String, String> d = new Task<>();
        d.setJob(i -> {
            try {
                Thread.sleep(1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        Task<String, String> e = new Task<>();
        e.setJob(i -> {
            try {
                Thread.sleep(1000);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("all jobs done");
            return null;
        });
        a.addNext(b);
        b.addNext(e, d);
        c.addNext(b, d);
        d.addNext(e);
        TaskFlow taskFlow = new TaskFlow();
        taskFlow.registerTasks(a, b, c, d, e);
        long oldtime = System.currentTimeMillis();
        taskFlow.start();
        System.out.println(System.currentTimeMillis() - oldtime);
    }
}