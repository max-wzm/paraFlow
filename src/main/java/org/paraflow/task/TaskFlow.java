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
    private ExecutorService                            executorService = Executors.newCachedThreadPool();
    private Map<String, Task>                          id2TaskMap      = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<TaskResult>> id2Future       = new ConcurrentHashMap<>();

    public void registerTasks(Task... tasks) {
        for (Task task : tasks) {
            if (!checkValidTask(task)) {
                throw new IllegalStateException("Invalid task");
            }
            id2TaskMap.put(task.getId(), task);
        }
    }

    private boolean checkValidTask(BaseTask task) {
        return true;
    }

    public boolean start() throws ExecutionException, InterruptedException {
        int maxLayer = computeLayer();
        Map<Integer, List<Task>> layer2Task = id2TaskMap.values()
                .stream()
                .collect(Collectors.groupingBy(BaseTask::getDepLayer));
        List<Task> initTasks = layer2Task.get(0);
        for (Task t : initTasks) {
            CompletableFuture<TaskResult> future = CompletableFuture.supplyAsync(() -> {
                Object obj = t.execute(new HashMap<>());

                TaskResult result = t.getResultWrapper();
                result.setResult(obj);
                System.out.println("fini");
                return result;
            }, executorService);
            id2Future.put(t.getId(), future);
        }
        for (int i = 1; i <= maxLayer; i++) {
            List<Task> layerTasks = layer2Task.get(i);
            for (Task t : layerTasks) {
                CompletableFuture[] dep = t.getPrevTaskIds()
                        .stream()
                        .map(id2Future::get)
                        .toArray(CompletableFuture[]::new);
                CompletableFuture<TaskResult> future = CompletableFuture.allOf(dep).thenApplyAsync(v -> {
                    Map<String, TaskResult> id2Result = buildId2ResultMap(t);
                    Object obj = t.execute(id2Result);
                    TaskResult result = t.getResultWrapper();
                    result.setResult(obj);
                    return result;
                }, executorService);
                id2Future.put(t.getId(), future);
            }
        }
        CompletableFuture.allOf(id2Future.values().toArray(new CompletableFuture[0])).get();
        return true;
    }

    public int computeLayer() {
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
        return maxLayer;
    }

    private Map<String, TaskResult> buildId2ResultMap(Task task) {
        return task.getPrevTaskIds().stream().collect(Collectors.toMap(id -> id, id -> id2Future.get(id).join()));
    }
}
