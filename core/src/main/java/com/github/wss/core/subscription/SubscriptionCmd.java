package com.github.wss.core.subscription;

/**
 * 订阅命令
 *
 * @author wang xiao
 * date 2022/5/11
 */
public interface SubscriptionCmd {

    /**
     * 订阅id
     *
     * @return int
     */
    int getSubId();

    /**
     * 设置订阅id
     *
     * @param subId 订阅id
     */
    void setSubId(int subId);

    /**
     * 是否取消订阅
     *
     * @return boolean
     */
    boolean isUnSub();

    /**
     * 第一次是否查询数据库
     *
     * @return boolean
     */
    boolean isFirstQuery();


}
