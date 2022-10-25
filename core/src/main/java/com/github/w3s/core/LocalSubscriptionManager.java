package com.github.w3s.core;

import com.github.w3s.core.subscription.SubscriptionDataUpdate;

/**
 * 订阅管理
 *
 * @author wang xiao
 * date 2022/5/11
 */
public interface LocalSubscriptionManager {

    /**
     * 增加订阅消息
     *
     * @param subscription 订阅
     */
    void addSubscription(SubscriptionMsg subscription);

    /**
     * 移除订阅消息
     *
     * @param entityId      entityId
     * @param subscriptionId 订阅id
     */
    void cancelSubscription(String entityId, int subscriptionId);

    /**
     * 取消session 的所有订阅
     *
     * @param entityId entityId
     */
    void cancelAllSessionSubscriptions(String entityId);

    /**
     * 订阅 数据更新
     *
     * @param entityId entityId
     * @param update    更新数据
     * @param callback  回调
     * @param <T>       回调类型
     */
    <T> void onSubscriptionUpdate(String entityId, SubscriptionDataUpdate update, ServiceCallback<T> callback);
}
