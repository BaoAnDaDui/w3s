package com.github.baoan.service;

import com.github.baoan.data.SubscriptionMsg;
import com.github.baoan.subscription.SubscriptionDataUpdate;
import com.github.baoan.util.ServiceCallback;

/**
 * 订阅service
 * @author wang xiao
 * @date 2022/5/11
 */
public interface LocalSubscriptionService {

    /**
     * 增加订阅消息
     * @param subscription 订阅
     */
    void addSubscription(SubscriptionMsg subscription);

    /**
     * 移除订阅消息
     * @param sessionId session id
     * @param subscriptionId 订阅id
     */
    void cancelSubscription(String sessionId, int subscriptionId);

    /**
     * 取消session 的所有订阅
     * @param sessionId session id
     */
    void cancelAllSessionSubscriptions(String sessionId);

    /**
     * 订阅 数据更新
     * @param sessionId session is
     * @param update 更新数据
     * @param callback 回调
     * @param <T> 回调类型
     */
     <T> void onSubscriptionUpdate(String sessionId, SubscriptionDataUpdate update, ServiceCallback<T> callback);
}
