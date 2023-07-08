package org.paraflow.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.paraflow.action.Callback;
import org.paraflow.action.Job;

import java.util.Map;

/**
 * @author wangzhiming
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Task<I, O> extends BaseTask {
    private I              param;
    private TaskResult<O>  resultWrapper = new TaskResult<>();
    private Job<I, O>      job           = i -> null;
    private Callback<I, O> callback;

    public Task() {
    }

    public Task(String id) {
        super(id);
    }

    public O execute() {
        return job.doJob(param);
    }

    public void setJob(I param, Job<I, O> job) {
        this.job = job;
        this.param = param;
    }

}
