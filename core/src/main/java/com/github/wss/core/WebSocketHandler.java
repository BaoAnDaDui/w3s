package com.github.wss.core;

import com.github.wss.core.data.SessionEvent;
import com.github.wss.core.data.WebSocketMsgType;
import com.github.wss.core.data.WebSocketSessionRef;
import com.github.wss.core.msg.WebSocketMsg;
import com.github.wss.core.msg.WebSocketPingMsg;
import com.github.wss.core.msg.WebSocketTextMsg;
import com.github.wss.core.service.WebSocketAuthService;
import com.github.wss.core.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.github.wss.core.data.WebSocketConstant.*;

/**
 * web socket 处理类
 * @author wang xiao
 * date 2022/5/11
 */
@Service
public class WebSocketHandler extends TextWebSocketHandler implements WebSocketMsgEndpoint {


    private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);


    /**
     * 内部是 webSocket session id 与 session 引用
     */
    private static final ConcurrentMap<String, SessionMetaData> INTERNAL_SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 外部是 session 引用id 与 webSocket session id
     */
    private static final ConcurrentMap<String, String> EXTERNAL_SESSION_MAP = new ConcurrentHashMap<>();

    private WebSocketAuthService webSocketAuthServer;


    private WebSocketService webSocketService;




    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        SessionMetaData sessionMd = INTERNAL_SESSION_MAP.get(session.getId());
        if (sessionMd != null) {
            processSessionEvent(sessionMd.sessionRef, SessionEvent.onError(exception));
        }
    }



    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        try {
            if (session instanceof NativeWebSocketSession) {
                Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
                if (nativeSession != null) {
                    nativeSession.getAsyncRemote().setSendTimeout(SEND_TIMEOUT);
                }
            }
            String internalSessionId = session.getId();
            WebSocketSessionRef sessionRef = toRef(session);
            String externalSessionId = sessionRef.getSessionId();
            if (!webSocketAuthServer.checkLimits(internalSessionId, sessionRef.getUserId())) {
                return;
            }
            INTERNAL_SESSION_MAP.put(internalSessionId, new SessionMetaData(session, sessionRef, 1000));
            EXTERNAL_SESSION_MAP.put(externalSessionId, internalSessionId);
            processSessionEvent(sessionRef, SessionEvent.onEstablished());
            logger.info(" Session :{} is opened,user id:{} ,external session id:{}", internalSessionId, sessionRef.getUserId(),externalSessionId);
        } catch (InvalidParameterException e) {
            logger.warn("[{}] Failed to start session", session.getId(), e);
            session.close(CloseStatus.BAD_DATA.withReason(e.getMessage()));
        } catch (Exception e) {
            logger.warn("[{}] Failed to start session", session.getId(), e);
            session.close(CloseStatus.SERVER_ERROR.withReason(e.getMessage()));
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            SessionMetaData sessionMd = INTERNAL_SESSION_MAP.get(session.getId());
            if (sessionMd != null) {
                webSocketService.handleWebSocketMsg(sessionMd.sessionRef, message.getPayload());
            } else {
                session.close(CloseStatus.SERVER_ERROR.withReason("Session not found!"));
            }
        } catch (IOException e) {
            logger.warn("IO error", e);
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        try {
            SessionMetaData sessionMd = INTERNAL_SESSION_MAP.get(session.getId());
            if (sessionMd != null) {
                sessionMd.processPongMessage(System.currentTimeMillis());
            } else {
                session.close(CloseStatus.SERVER_ERROR.withReason("Session not found!"));
            }
        } catch (IOException e) {
            logger.warn("IO error", e);
        }
    }


    @Override
    public void send(WebSocketSessionRef sessionRef, String msg) {
        String externalId = sessionRef.getSessionId();
        String internalId = EXTERNAL_SESSION_MAP.get(externalId);
        if (internalId != null) {
            SessionMetaData sessionMd = INTERNAL_SESSION_MAP.get(internalId);
            if (sessionMd != null) {
                sessionMd.sendMsg(msg);
            }
        }
    }

    @Override
    public void sendPing(WebSocketSessionRef sessionRef, long currentTime) {
        String externalId = sessionRef.getSessionId();
        String internalId = EXTERNAL_SESSION_MAP.get(externalId);
        if (internalId != null) {
            SessionMetaData sessionMd = INTERNAL_SESSION_MAP.get(internalId);
            if (sessionMd != null) {
                sessionMd.sendPing(currentTime);
            }
        }
    }

    @Override
    public void close(WebSocketSessionRef sessionRef, CloseStatus withReason) throws IOException {
        String externalId = sessionRef.getSessionId();
        String internalId = EXTERNAL_SESSION_MAP.get(externalId);
        if (internalId != null) {
            SessionMetaData sessionMd = INTERNAL_SESSION_MAP.get(internalId);
            if (sessionMd != null) {
                sessionMd.session.close(withReason);
            }
        }
    }

    private WebSocketSessionRef toRef(WebSocketSession session) {
        URI sessionUri = session.getUri();
        assert sessionUri != null;
        String path = sessionUri.getPath();
        path = path.substring(WS_PLUGIN_PREFIX.length());
        if (path.length() == 0) {
            throw new IllegalArgumentException("URL should contain plugin token!");
        }
        Long userId = webSocketAuthServer.authAndReturnUserId(path);
        return new WebSocketSessionRef(UUID.randomUUID().toString(),userId,session.getLocalAddress(),session.getRemoteAddress());
    }


    private void processSessionEvent(WebSocketSessionRef sessionRef, SessionEvent event) {
        try {
            webSocketService.handleWebSocketSessionEvent(sessionRef, event);
        } catch (BeanCreationNotAllowedException e) {
            logger.warn("[{}] Failed to close session due to possible shutdown state", sessionRef.getSessionId());
        }
    }


    @Autowired
    public void setWebSocketAuthServer(WebSocketAuthService webSocketAuthServer) {
        this.webSocketAuthServer = webSocketAuthServer;
    }

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    private class SessionMetaData implements SendHandler {
        private final WebSocketSession session;
        private final RemoteEndpoint.Async asyncRemote;
        private final WebSocketSessionRef sessionRef;

        private volatile boolean isSending = false;
        private final Queue<WebSocketMsg<?>> msgQueue;

        private volatile long lastActivityTime;

        SessionMetaData(WebSocketSession session, WebSocketSessionRef sessionRef, int maxMsgQueuePerSession) {
            super();
            this.session = session;
            Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
            this.asyncRemote = nativeSession.getAsyncRemote();
            this.sessionRef = sessionRef;
            this.msgQueue = new LinkedBlockingQueue<>(maxMsgQueuePerSession);
            this.lastActivityTime = System.currentTimeMillis();
        }

        synchronized void sendPing(long currentTime) {
            try {
                long timeSinceLastActivity = currentTime - lastActivityTime;
                if (timeSinceLastActivity >= PING_TIMEOUT) {
                    logger.warn(" ping timeout will be Closing session :{}", session.getId());
                    closeSession(CloseStatus.SESSION_NOT_RELIABLE);
                } else if (timeSinceLastActivity >= PING_TIMEOUT / NUMBER_OF_PING_ATTEMPTS) {
                    sendMsg(WebSocketPingMsg.INSTANCE);
                }
            } catch (Exception e) {
                logger.trace("[{}] Failed to send ping msg", session.getId(), e);
                closeSession(CloseStatus.SESSION_NOT_RELIABLE);
            }
        }

        private void closeSession(CloseStatus reason) {
            try {
                close(this.sessionRef, reason);
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
    }


}
