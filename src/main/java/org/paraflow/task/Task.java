package org.paraflow.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.paraflow.action.Job;
import org.paraflow.action.TaskCallback;

/**
 * @author wangzhiming
 */
@EqualsAndHashCode(callSuper = true)
@Data
class Task<I, O> extends BaseTask {
    private I             param;
    private TaskResult<O> resultWrapper = new TaskResult<>();
    private Job<I, O>     job           = i -> null;
    private boolean       abortIfFailed = false;

    private TaskCallback<O> callback = new TaskCallback<O>() {
        @Override
        public O onSuccess(O result) {
            return result;
        }

        @Override
        public O onFailure(O result, Throwable exception) {
            return result;
        }
    };

    public Task() {
    }

    public Task(String id) {
        super(id);
    }

    public Task(String id, TaskCallback<O> callback) {
        super(id);
        this.callback = callback;
    }

    TaskResult<O> execute() {
        resultWrapper.setResult(job.doJob(param));
        return resultWrapper;
    }

    TaskResult<O> onFailure(Throwable exception) {
        resultWrapper.setState(TaskStateEnum.FAILURE);
        resultWrapper.setResult(callback.onFailure(resultWrapper.getResult(), exception));
        return resultWrapper;
    }

    TaskResult<O> onSuccess() {
        resultWrapper.setState(TaskStateEnum.SUCCESS);
        resultWrapper.setResult(callback.onSuccess(resultWrapper.getResult()));
        return resultWrapper;
    }

    public void setJob(I param, Job<I, O> job) {
        this.job = job;
        this.param = param;
    }

    public O getTaskResult() {
        return resultWrapper.getResult();
    }

}
