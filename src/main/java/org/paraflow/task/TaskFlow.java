package org.paraflow.task;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author wangzhiming
 */
@Data
public class TaskFlow {
    private ExecutorService                   executorService = Executors.newCachedThreadPool();
    private Map<String, Task<?, ?>>           id2TaskMap      = new ConcurrentHashMap<>();
    private Map<String, CompletableFuture<?>> id2Future       = new ConcurrentHashMap<>();

    public void registerTasks(Task... tasks) {
        for (Task task : tasks) {
            if (!checkValidTask(task)) {
                throw new IllegalStateException("Invalid task");
            }
            id2TaskMap.put(task.getId(), task);
        }
    }

    private boolean checkValidTask(Task task) {
        return true;
    }

    public boolean start() throws ExecutionException, InterruptedException {
        computeLayer();
        Map<Integer, List<Task<?, ?>>> layer2Task = id2TaskMap.values()
                .stream()
                .collect(Collectors.groupingBy(Task::getLayer));
        List<Task<?, ?>> initTasks = layer2Task.get(0);
        for (Task<?, ?> t : initTasks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(t::execute);
            id2Future.put(t.getId(), future);
        }
        for (int i = 1; i < layer2Task.size(); i++) {
            if (!layer2Task.containsKey(i)) {
                break;
            }
            List<Task<?, ?>> layerTasks = layer2Task.get(i);
            for (Task<?, ?> t : layerTasks) {
                CompletableFuture[] dep = t.getPrevTaskIds()
                        .stream()
                        .map(id2Future::get)
                        .toArray(CompletableFuture[]::new);
                CompletableFuture<Void> future = CompletableFuture.allOf(dep).thenRunAsync(t::execute);
                id2Future.put(t.getId(), future);
            }
        }
        CompletableFuture.allOf(id2Future.values().toArray(new CompletableFuture[0])).get();
        return true;
    }

    public void computeLayer() {
        Queue<Task> q = id2TaskMap.values()
                .stream()
                .filter(task -> task.getPrevTaskIds().isEmpty())
                .collect(Collectors.toCollection(LinkedList::new));
        while (!q.isEmpty()) {
            Task task = q.poll();
            int layer = task.getLayer();
            List<Task> nextTasks = task.getNextTasks();
            for (Task nextTask : nextTasks) {
                nextTask.setLayer(Math.max(layer + 1, nextTask.getLayer()));
                nextTask.setIn(nextTask.getIn() - 1);
                if (nextTask.getIn() == 0) {
                    q.offer(nextTask);
                }
            }
        }
    }
}
