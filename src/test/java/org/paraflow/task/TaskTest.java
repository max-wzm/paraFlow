package org.paraflow.task;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class TaskTest {
    @Test
    public void testCompleteFuture() throws ExecutionException, InterruptedException {
        Task<Object, Object> a = new Task<>("A");
        a.setRunnable(() -> System.out.println(a.getId()));
        Task<String, String> b = new Task<>("B");
        b.setRunnable(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(b.getId());
        });
        Task<Integer, String> c = new Task<>("C");
        c.setRunnable(() -> System.out.println(c.getId()));
        a.addNext(b);
        a.addNext(c);
        b.addNext(c);
        TaskFlow taskFlow = new TaskFlow();
        taskFlow.registerTasks(a, b, c);
        taskFlow.getId2TaskMap().forEach((key, value) -> System.out.println(key + ": " + value.getLayer()));
        taskFlow.start();
    }
}