package com.github.w3s.core;

/**
 * 服务回调
 *
 * @author wang xiao
 * date 2022/5/11
 */
public interface ServiceCallback<T> {


    /**
     * 成功
     *
     * @param result 结果
     */
    void onSuccess(T result);

    /**
     * 失败
     *
     * @param throwable 失败原因
     */
    void onFailure(Throwable throwable);

}
