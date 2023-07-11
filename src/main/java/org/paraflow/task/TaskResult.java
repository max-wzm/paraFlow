package org.paraflow.task;

import lombok.Data;

/**
 * @author wangzhiming
 */
@Data
public class TaskResult<T> {
    private TaskStateEnum state = TaskStateEnum.INIT;
    private T             result;
}
