package org.paraflow.action;

/**
 * @author wangzhiming
 */
public interface Job<I, O> {
    O doJob(I param);
}
