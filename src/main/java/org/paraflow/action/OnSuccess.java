package org.paraflow.action;

/**
 * @author wangzhiming
 */
public interface OnSuccess<I, O> {
    O doCallback(I param);
}