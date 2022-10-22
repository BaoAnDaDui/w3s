package com.github.wss.core;

import com.github.wss.core.data.WebSocketSessionRef;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;

/**
 * web socket endpoint
 *
 * @author wang xiao
 * date 2022/5/11
 */
public interface WebSocketMsgEndpoint {


    /**
     * 发送 数据
     *
     * @param sessionRef sessionRef 引用确定发送到那个socket
     * @param msg        json 消息
     * @throws IOException IOException
     */
    void send(WebSocketSessionRef sessionRef, String msg) throws IOException;

    /**
     * ping
     *
     * @param sessionRef  sessionRef 引用确定发送到那个socket
     * @param currentTime 当前
     * @throws IOException IOException
     */
    void sendPing(WebSocketSessionRef sessionRef, long currentTime) throws IOException;

    /**
     * ping
     *
     * @param sessionRef sessionRef 引用确定发送到那个socket
     * @param withReason 关闭原因
     * @throws IOException IOException
     */
    void close(WebSocketSessionRef sessionRef, CloseStatus withReason) throws IOException;
}
