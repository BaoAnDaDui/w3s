package com.github.wss.core.subscription;

/**
 * 订阅数据更新
 *
 * @author wang xiao
 * date 2022/5/11
 */
public interface SubscriptionDataUpdate {

    /**
     * 订阅id
     *
     * @return int
     */
    int getSubId();

    /**
     * 订阅数据
     *
     * @return object
     */
    Object getData();
}
