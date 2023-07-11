package org.paraflow.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.paraflow.action.Job;
import org.paraflow.action.OnFailure;
import org.paraflow.action.OnSuccess;

/**
 * @author wangzhiming
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Task<I, O> extends BaseTask {
    private I               param;
    private TaskResult<O>   resultWrapper = new TaskResult<>();
    private Job<I, O>       job           = i -> null;
    private OnSuccess<I, O> onSuccess     = i -> null;
    private OnFailure<I, O> onFailure     = i -> null;
    private boolean         abortIfFailed = false;
    // exceptionPolicy: abortFlow, ignore, self-define

    public Task() {
    }

    public Task(String id) {
        super(id);
    }

    public O execute() {
        return job.doJob(param);
    }

    public O onFailure() {
        return onFailure.doCallback(param);
    }

    public O onSuccess() {
        return onSuccess.doCallback(param);
    }

    public void setJob(I param, Job<I, O> job) {
        this.job = job;
        this.param = param;
    }

}
