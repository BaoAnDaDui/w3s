package com.github.wss.core.subscription;

/**
 * 订阅id
 *
 * @author wang xiao
 * date 2022/5/11
 */
public abstract class AbstractSubscriptionCmd implements SubscriptionCmd {

    private int subId;

    private String entityId;

    private String entityType;


    private boolean unSub;


    @Override
    public int getSubId() {
        return subId;
    }

    @Override
    public void setSubId(int subId) {
        this.subId = subId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @Override
    public boolean isUnSub() {
        return unSub;
    }

    public void setUnSub(boolean unSub) {
        this.unSub = unSub;
    }


    @Override
    public String toString() {
        return "AbstractSubscriptionCmd{" +
                "subId=" + subId +
                ", entityId='" + entityId + '\'' +
                ", entityType='" + entityType + '\'' +
                ", unSub=" + unSub +
                '}';
    }
}
