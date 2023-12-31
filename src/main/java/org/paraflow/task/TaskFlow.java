package org.paraflow.task;

import lombok.Data;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author wangzhiming
 */
@Data
public class TaskFlow {
    private       ExecutorService                            executorService = Executors.newCachedThreadPool();
    private       Map<String, Task>                          id2TaskMap      = new ConcurrentHashMap<>();
    private       Map<String, CompletableFuture<TaskResult>> id2Future       = new ConcurrentHashMap<>();
    private final Object                                     lock            = new Object();

    public void registerTasks(Task... tasks) {
        Queue<BaseTask> q = new LinkedList<>(Arrays.asList(tasks));
        while (!q.isEmpty()) {
            Queue<BaseTask> p = new LinkedList<>();
            while (!q.isEmpty()) {
                BaseTask out = q.poll();
                if (id2TaskMap.containsKey(out.getId())) {
                    continue;
                }
                id2TaskMap.put(out.getId(), (Task) out);
                out.getNextTasks().forEach(p::offer);
            }
            q = p;
        }
    }

    private boolean checkValidTask(BaseTask task) {
        return true;
    }

    public boolean start() {
        Map<Integer, List<Task>> layer2Task = initDependLayer();
        int maxLayer = layer2Task.size() - 1;

        for (int i = 0; i <= maxLayer; i++) {
            List<Task> layerTasks = layer2Task.get(i);
            for (Task task : layerTasks) {
                CompletableFuture[] deps = task.getPrevTaskIds()
                        .stream()
                        .map(id2Future::get)
                        .toArray(CompletableFuture[]::new);

                CompletableFuture<TaskResult> future = CompletableFuture.allOf(deps).thenApplyAsync(v -> {
                    System.out.println("start excute task");
                    return task.execute();
                }, executorService).handle((resWrapper, e) -> {
                    if (e != null) {
                        resWrapper = task.onFailure(e.getCause());
                        if (task.isAbortIfFailed()) {
                            executorService.execute(this::abort);
                        }
                        return resWrapper;
                    }
                    return task.onSuccess();
                });
                id2Future.put(task.getId(), future);
            }
        }
        synchronized (lock) {
            lock.notifyAll();
        }
        System.out.println("print sth");
        try {
            CompletableFuture.allOf(id2Future.values().toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void abort() {
        synchronized (lock) {
            while (id2Future.size() != id2TaskMap.size()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            id2Future.values().forEach(f -> f.cancel(true));
        }

    }

    private Map<Integer, List<Task>> initDependLayer() {
        int maxLayer = 0;
        Queue<BaseTask> q = id2TaskMap.values()
                .stream()
                .filter(task -> task.getPrevTaskIds().isEmpty())
                .collect(Collectors.toCollection(LinkedList::new));
        while (!q.isEmpty()) {
            BaseTask task = q.poll();
            int layer = task.getDepLayer();
            maxLayer = Math.max(maxLayer, layer);
            List<BaseTask> nextTasks = task.getNextTasks();
            for (BaseTask nextTask : nextTasks) {
                nextTask.setDepLayer(Math.max(layer + 1, nextTask.getDepLayer()));
                nextTask.setInDegree(nextTask.getInDegree() - 1);
                if (nextTask.getInDegree() == 0) {
                    q.offer(nextTask);
                }
            }
        }
        id2TaskMap.forEach((key, value) -> System.out.println(key + ": " + value.getDepLayer()));
        return id2TaskMap.values().stream().collect(Collectors.groupingBy(BaseTask::getDepLayer));
    }

    public static void sleepIgnoreInterrupt(int duration, TimeUnit timeUnit) {
        try {
            Thread.sleep(timeUnit.toMillis(duration));
        } catch (InterruptedException ignored) {

        }
    }
}
