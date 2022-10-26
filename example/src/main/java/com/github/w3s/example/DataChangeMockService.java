package com.github.w3s.example;

import com.github.w3s.core.LocalSubscriptionManager;
import com.github.w3s.core.ServiceCallback;
import com.github.w3s.core.subscription.DefaultSubscriptionDataUpdate;
import com.github.w3s.core.subscription.SubscriptionDataUpdate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 数据模拟服务 一般是 rabbit 设备数据推送 保存接口等触发
 *
 * @author wang xiao
 * date 2022/10/26
 */
@Service
public class DataChangeMockService implements InitializingBean {

    @Autowired
    private LocalSubscriptionManager subscriptionManager;


    private void changeData() {
        SubscriptionDataUpdate subscriptionDataUpdate = new DefaultSubscriptionDataUpdate(1, "changeData");
        subscriptionManager.onSubscriptionUpdate("1", subscriptionDataUpdate, new ServiceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                System.out.println("send ok");
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("failure: " + throwable.toString());
            }
        });
    }

    @Override
    public void afterPropertiesSet() {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        threadPoolExecutor.scheduleAtFixedRate(this::changeData, 10, 10, TimeUnit.SECONDS);
    }
}
