package org.paraflow.action;

import org.paraflow.task.TaskResult;

public interface TaskCallback<O> {
    O onSuccess(O result);

    O onFailure(O result, Throwable exception);
}
