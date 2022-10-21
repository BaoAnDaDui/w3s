package com.github.wss.core.data;

import com.github.wss.core.subscription.SubscriptionDataUpdate;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author wang xiao
 * date 2022/5/11
 */
public class SubscriptionMsg {

    private final String sessionId;
    private final int subId;

    private final String entityId;

    private final BiConsumer<String, SubscriptionDataUpdate> updateConsumer;


    public SubscriptionMsg( String sessionId, int subId, String entityId, BiConsumer<String, SubscriptionDataUpdate> updateConsumer) {
        this.sessionId = sessionId;
        this.subId = subId;
        this.entityId = entityId;
        this.updateConsumer = updateConsumer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubscriptionMsg that = (SubscriptionMsg) o;

        if (subId != that.subId) {
            return false;
        }
        if (!Objects.equals(sessionId, that.sessionId)) {
            return false;
        }
        if (!Objects.equals(entityId, that.entityId)) {
            return false;
        }
        return Objects.equals(updateConsumer, that.updateConsumer);
    }

    @Override
    public int hashCode() {
        int result = sessionId != null ? sessionId.hashCode() : 0;
        result = 31 * result + subId;
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        result = 31 * result + (updateConsumer != null ? updateConsumer.hashCode() : 0);
        return result;
    }


    public String getSessionId() {
        return sessionId;
    }

    public int getSubId() {
        return subId;
    }

    public String getEntityId() {
        return entityId;
    }

    public BiConsumer<String, SubscriptionDataUpdate> getUpdateConsumer() {
        return updateConsumer;
    }

    public static  final class SubscriptionMsgBuilder{
        private  String sessionId;
        private  int subId;

        private  String entityId;

        private  BiConsumer<String, SubscriptionDataUpdate> consumer;



        public SubscriptionMsgBuilder sessionId(String sessionId){
            this.sessionId = sessionId;
            return this;
        }

        public SubscriptionMsgBuilder subId(int subId){
            this.subId = subId;
            return this;
        }

        public SubscriptionMsgBuilder entityId(String entityId){
            this.entityId = entityId;
            return this;
        }

        public SubscriptionMsgBuilder consumer(BiConsumer<String, SubscriptionDataUpdate> consumer){
            this.consumer = consumer;
            return this;
        }

        public SubscriptionMsg build() {
            return new SubscriptionMsg(this.sessionId,this.subId,this.entityId,this.consumer);
        }
        public  static   SubscriptionMsgBuilder  builder(){
            return new SubscriptionMsgBuilder() ;
        }
    }
}
