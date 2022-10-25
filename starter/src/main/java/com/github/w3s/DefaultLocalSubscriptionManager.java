package com.github.w3s;



import com.github.w3s.core.LocalSubscriptionManager;
import com.github.w3s.core.ServiceCallback;
import com.github.w3s.core.SubscriptionMsg;
import com.github.w3s.core.subscription.SubscriptionDataUpdate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author wang xiao
 * date 2022/5/11
 */

public class DefaultLocalSubscriptionManager implements LocalSubscriptionManager {

    private final Map<String, Map<Integer, SubscriptionMsg>> subscriptionsByEntityId = new ConcurrentHashMap<>();

    private ExecutorService subscriptionUpdateExecutor;

    @PostConstruct
    public void initExecutor() {
        subscriptionUpdateExecutor = new ThreadPoolExecutor(4, 100, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void addSubscription(SubscriptionMsg subscription) {
        registerSubscription(subscription);
    }

    @Override
    public void cancelSubscription(String entityId, int subscriptionId) {
        Map<Integer, SubscriptionMsg> sessionSubscriptions = subscriptionsByEntityId.get(entityId);
        if (sessionSubscriptions != null) {
            SubscriptionMsg subscription = sessionSubscriptions.remove(subscriptionId);
            if (subscription != null && sessionSubscriptions.isEmpty()) {
                subscriptionsByEntityId.remove(entityId);
            }
        }
    }

    @Override
    public void cancelAllSessionSubscriptions(String entityId) {
        Map<Integer, SubscriptionMsg> subscriptions = subscriptionsByEntityId.get(entityId);
        if (subscriptions != null) {
            Set<Integer> toRemove = new HashSet<>(subscriptions.keySet());
            toRemove.forEach(id -> cancelSubscription(entityId, id));
        }
    }

    @Override
    public <T> void onSubscriptionUpdate(String entityId, SubscriptionDataUpdate update, ServiceCallback<T> callback) {
        SubscriptionMsg subscription = subscriptionsByEntityId
                .getOrDefault(entityId, Collections.emptyMap()).get(update.getSubId());
        if (subscription != null) {
            subscriptionUpdateExecutor.submit(() -> subscription.getUpdateConsumer().accept(entityId, update));
        }
        callback.onSuccess(null);
    }

    private void registerSubscription(SubscriptionMsg subscription) {
        Map<Integer, SubscriptionMsg> sessionSubscriptions = subscriptionsByEntityId.computeIfAbsent(subscription.getEntityId(), k -> new ConcurrentHashMap<>(20));
        sessionSubscriptions.put(subscription.getSubId(), subscription);
    }

}
