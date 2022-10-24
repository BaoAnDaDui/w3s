package com.github.w3s.core.msg;

/**
 * web socket 消息
 *
 * @author wang xiao
 * date 2022/5/11
 */
public interface WebSocketMsg<T> {

    /**
     * 获取消息类型
     *
     * @return WebSocketMsgType
     */
    WebSocketMsgType getMsgType();

    /**
     * 获取消息
     *
     * @return T 消息
     */
    T getMsg();
}
