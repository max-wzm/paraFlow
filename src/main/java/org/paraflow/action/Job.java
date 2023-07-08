package org.paraflow.action;

import org.paraflow.task.TaskResult;

import java.util.Map;

/**
 * @author wangzhiming
 */
@FunctionalInterface
public interface Job<I, O> {
    O doJob(I param);
}
