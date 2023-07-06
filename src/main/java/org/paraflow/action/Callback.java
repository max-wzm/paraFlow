package org.paraflow.action;

import org.paraflow.task.TaskResult;

public interface Callback<I, O> {
    void onSuccess(I param, TaskResult<O> result);
    void onFailure(I param, TaskResult<O> result);
}
