package org.paraflow.task;

import lombok.Data;

/**
 * @author wangzhiming
 */
@Data
public class TaskResult<T> {
    private TaskStateEnum state;
    private T             result;
}
