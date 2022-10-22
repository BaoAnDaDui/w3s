package com.github.wss.core;

import com.github.wss.core.data.WebSocketMsgType;
import com.github.wss.core.data.WebSocketSessionRef;
import com.github.wss.core.msg.WebSocketMsg;
import com.github.wss.core.msg.WebSocketPingMsg;
import com.github.wss.core.msg.WebSocketTextMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;

import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.github.wss.core.data.WebSocketConstant.NUMBER_OF_PING_ATTEMPTS;
import static com.github.wss.core.data.WebSocketConstant.PING_TIMEOUT;

/**
 * @author wang xiao
 * date 2022/10/22
 */
public class SessionMetaData implements SendHandler {

    private final Logger logger = LoggerFactory.getLogger(SessionMetaData.class);
    private final WebSocketSession session;
    private final RemoteEndpoint.Async asyncRemote;
    private final WebSocketSessionRef sessionRef;

    private final Queue<WebSocketMsg<?>> msgQueue;

    private final WebSocketMsgEndpoint webSocketMsgEndpointRef;

    private volatile boolean isSending = false;


    private volatile long lastActivityTime;

    SessionMetaData(WebSocketSession session, WebSocketSessionRef sessionRef, WebSocketMsgEndpoint webSocketMsgEndpointRef, int maxMsgQueuePerSession) {
        super();
        this.session = session;
        Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
        assert nativeSession != null;
        this.asyncRemote = nativeSession.getAsyncRemote();
        this.sessionRef = sessionRef;
        this.msgQueue = new LinkedBlockingQueue<>(maxMsgQueuePerSession);
        this.lastActivityTime = System.currentTimeMillis();
        this.webSocketMsgEndpointRef = webSocketMsgEndpointRef;
    }

    synchronized void sendPing(long currentTime) {
        try {
            long timeSinceLastActivity = currentTime - lastActivityTime;
            if (timeSinceLastActivity >= PING_TIMEOUT) {
                logger.warn(" ping timeout will be Closing session :{}", session.getId());
                closeSession(CloseStatus.SESSION_NOT_RELIABLE);
            } else if (timeSinceLastActivity >= PING_TIMEOUT / NUMBER_OF_PING_ATTEMPTS) {
                sendMsg(WebSocketPingMsg.INSTANCE);
            } else {
                // do nothing
            }
        } catch (Exception e) {
            logger.trace("[{}] Failed to send ping msg", session.getId(), e);
            closeSession(CloseStatus.SESSION_NOT_RELIABLE);
        }
    }

    private void closeSession(CloseStatus reason) {
        try {
            webSocketMsgEndpointRef.close(this.sessionRef, reason);
        } catch (IOException ioe) {
            logger.trace("[{}] Session transport error", session.getId(), ioe);
        }
    }

    synchronized void processPongMessage(long currentTime) {
        lastActivityTime = currentTime;
    }

    synchronized void sendMsg(String msg) {
        sendMsg(new WebSocketTextMsg(msg));
    }

    synchronized void sendMsg(WebSocketMsg<?> msg) {
        if (isSending) {
            try {
                msgQueue.add(msg);
            } catch (RuntimeException e) {
                logger.error("Session:{} closed due to queue error: {}", session.getId(), e);
                closeSession(CloseStatus.POLICY_VIOLATION.withReason("Max pending updates limit reached!"));
            }
        } else {
            isSending = true;
            sendMsgInternal(msg);
        }
    }

    private void sendMsgInternal(WebSocketMsg<?> msg) {
        try {
            if (WebSocketMsgType.TEXT.equals(msg.getMsgType())) {
                WebSocketTextMsg textMsg = (WebSocketTextMsg) msg;
                this.asyncRemote.sendText(textMsg.getMsg(), this);
            } else {
                WebSocketPingMsg pingMsg = (WebSocketPingMsg) msg;
                this.asyncRemote.sendPing(pingMsg.getMsg());
                processNextMsg();
            }
        } catch (Exception e) {
            logger.error("Session:{} Failed to send msg, error is: {}", session.getId(), e);
            closeSession(CloseStatus.SESSION_NOT_RELIABLE);
        }
    }

    @Override
    public void onResult(SendResult result) {
        if (!result.isOK()) {
            logger.error("Session:{} send message is  error: {}", session.getId(), result.getException());
            closeSession(CloseStatus.SESSION_NOT_RELIABLE);
        } else {
            processNextMsg();
        }
    }

    private void processNextMsg() {
        WebSocketMsg<?> msg = msgQueue.poll();
        if (msg != null) {
            sendMsgInternal(msg);
        } else {
            isSending = false;
        }
    }

    public WebSocketSessionRef getSessionRef() {
        return sessionRef;
    }

    public WebSocketSession getSession() {
        return session;
    }
}

