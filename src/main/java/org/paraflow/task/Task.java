package org.paraflow.task;

import lombok.Data;
import org.paraflow.action.Callback;
import org.paraflow.action.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author wangzhiming
 */
@Data
public class Task<I, O> {
    private String           id;
    private Job<I, O>        job;
    private Callback<I, O>   callback;
    private Runnable         runnable;
    private List<String>     prevTaskIds = new ArrayList<>();
    private List<Task<?, ?>> nextTasks   = new ArrayList<>();
    private TaskStateEnum    state;
    private int              layer       = 0;
    private int              in          = 0;

    public void addNext(Task<?, ?>... next) {
        in += next.length;
        for (Task<?, ?> task : next) {
            nextTasks.add(task);
            task.addPrev(this);
        }
    }

    public void execute() {
        runnable.run();
    }

    public Task(String id) {
        this.id = id;
    }

    public void addPrev(Task<?, ?>... prev) {
        Arrays.stream(prev).forEach(pr -> prevTaskIds.add(pr.getId()));
    }

}
