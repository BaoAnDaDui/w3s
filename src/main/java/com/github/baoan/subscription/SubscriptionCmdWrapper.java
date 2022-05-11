package com.github.baoan.subscription;

import java.util.List;

/**
 * 定于装饰者
 * 这个类 可以扩张 覆盖 对应前端传递过来json
 * @author wang xiao
 * @date 2022/5/11
 */
public class SubscriptionCmdWrapper {

    private List<AbstractSubscriptionCmd> subs;


    public SubscriptionCmdWrapper(List<AbstractSubscriptionCmd> subs) {
        this.subs = subs;
    }

    public List<AbstractSubscriptionCmd> getSubs() {
        return subs;
    }

    public void setSubs(List<AbstractSubscriptionCmd> subs) {
        this.subs = subs;
    }
}
