package com.github.wss.core.service;

import com.github.wss.core.session.SessionEvent;
import com.github.wss.core.session.WebSocketSessionRef;
import com.github.wss.core.subscription.SubscriptionDataUpdate;
import org.springframework.web.socket.CloseStatus;

/**
 * @author wang xiao
 * date 2022/5/11
 */
public interface WebSocketService {


    /**
     * 处理session event
     *
     * @param sessionRef   session ref
     * @param sessionEvent session event
     */
    void handleWebSocketSessionEvent(WebSocketSessionRef sessionRef, SessionEvent sessionEvent);


    /**
     * 处理 websocket 消息
     *
     * @param sessionRef session ref
     * @param msg        msg
     */
    void handleWebSocketMsg(WebSocketSessionRef sessionRef, String msg);

    /**
     * 发送订阅 更新数据
     *
     * @param sessionId session id
     * @param update    更新数据
     */
    void sendWsMsg(String sessionId, SubscriptionDataUpdate update);


    /**
     * 关闭
     *
     * @param sessionId sessionId
     * @param status    CloseStatus
     */
    void close(String sessionId, CloseStatus status);
}
