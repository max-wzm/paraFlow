package org.paraflow.task;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author wangzhiming
 */
@Data
public class BaseTask {
    private String         id;
    private List<String>   prevTaskIds = new ArrayList<>();
    private List<BaseTask> nextTasks   = new ArrayList<>();
    private TaskStateEnum  state       = TaskStateEnum.INIT;
    private int            depLayer    = 0;
    private int            inDegree    = 0;

    public BaseTask() {
        this.id = UUID.randomUUID().toString();
    }

    public BaseTask(String id) {
        this.id = id;
    }

    public void addNext(BaseTask... next) {
        for (BaseTask task : next) {
            nextTasks.add(task);
            task.setInDegree(task.getInDegree() + 1);
            task.addPrev(this);
        }
    }

    public void addPrev(BaseTask... prev) {
        Arrays.stream(prev).forEach(pr -> prevTaskIds.add(pr.getId()));
    }

}
