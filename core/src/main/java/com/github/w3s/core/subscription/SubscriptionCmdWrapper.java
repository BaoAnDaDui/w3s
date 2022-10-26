package com.github.w3s.core.subscription;

import java.util.List;

/**
 * 定于装饰者
 * 这个类 可以扩张 覆盖 对应前端传递过来json
 *
 * @author wang xiao
 * date 2022/5/11
 */
public class SubscriptionCmdWrapper {

    private List<BaseSubscriptionCmd> subs;

    public SubscriptionCmdWrapper() {
    }

    public SubscriptionCmdWrapper(List<BaseSubscriptionCmd> subs) {
        this.subs = subs;
    }

    public List<BaseSubscriptionCmd> getSubs() {
        return subs;
    }

    public void setSubs(List<BaseSubscriptionCmd> subs) {
        this.subs = subs;
    }
}
