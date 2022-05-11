package com.github.baoan.service.impl;

import com.github.baoan.data.SubscriptionMsg;
import com.github.baoan.service.LocalSubscriptionService;
import com.github.baoan.subscription.SubscriptionDataUpdate;
import com.github.baoan.util.ServiceCallback;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author wang xiao
 * @date 2022/5/11
 */
@Service
public class DefaultLocalSubscriptionService implements LocalSubscriptionService {

    private final Map<String, Map<Integer, SubscriptionMsg>> subscriptionsBySessionId = new ConcurrentHashMap<>();

    private ExecutorService subscriptionUpdateExecutor;

    @PostConstruct
    public void initExecutor() {
        subscriptionUpdateExecutor = new ThreadPoolExecutor(4,100,60, TimeUnit.SECONDS,new SynchronousQueue<>(),new ThreadPoolExecutor.AbortPolicy());

    }

    @Override
    public void addSubscription(SubscriptionMsg subscription) {
        registerSubscription(subscription);
    }

    @Override
    public void cancelSubscription(String sessionId, int subscriptionId) {
        Map<Integer, SubscriptionMsg> sessionSubscriptions = subscriptionsBySessionId.get(sessionId);
        if (sessionSubscriptions != null) {
            SubscriptionMsg subscription = sessionSubscriptions.remove(subscriptionId);
            if (subscription != null) {
                if (sessionSubscriptions.isEmpty()) {
                    subscriptionsBySessionId.remove(sessionId);
                }
            }
        }
    }

    @Override
    public void cancelAllSessionSubscriptions(String sessionId) {
        Map<Integer, SubscriptionMsg> subscriptions = subscriptionsBySessionId.get(sessionId);
        if (subscriptions != null) {
            Set<Integer> toRemove = new HashSet<>(subscriptions.keySet());
            toRemove.forEach(id -> cancelSubscription(sessionId, id));
        }
    }

    @Override
    public <T> void onSubscriptionUpdate(String sessionId, SubscriptionDataUpdate update, ServiceCallback<T> callback) {
        SubscriptionMsg subscription = subscriptionsBySessionId
                .getOrDefault(sessionId, Collections.emptyMap()).get(update.getSubId());
        if (subscription != null) {
            subscriptionUpdateExecutor.submit(() -> subscription.getUpdateConsumer().accept(sessionId, update));
        }
        callback.onSuccess(null);
    }

    private void registerSubscription(SubscriptionMsg subscription) {
        Map<Integer, SubscriptionMsg> sessionSubscriptions = subscriptionsBySessionId.computeIfAbsent(subscription.getSessionId(), k -> new ConcurrentHashMap<>(20));
        sessionSubscriptions.put(subscription.getSubId(), subscription);
    }

}
