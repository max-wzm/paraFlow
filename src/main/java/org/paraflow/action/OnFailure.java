package org.paraflow.action;

/**
 * @author wangzhiming
 */
public interface OnFailure<I, O> {
    O doCallback(I param, Throwable exception);
}