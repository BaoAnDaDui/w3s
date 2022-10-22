package com.github.wss.core.subscription;

/**
 * @author wang xiao
 * date 2022/5/11
 */
public class DefaultSubscriptionDataUpdate implements SubscriptionDataUpdate {

    private final int subId;

    private final Object data;

    private final long ts;


    public DefaultSubscriptionDataUpdate(int subId, Object data) {
        this.subId = subId;
        this.data = data;
        this.ts = System.currentTimeMillis();
    }

    @Override
    public int getSubId() {
        return subId;
    }

    @Override
    public Object getData() {
        return data;
    }

    public long getTs() {
        return ts;
    }
}
