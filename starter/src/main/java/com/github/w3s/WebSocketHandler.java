package com.github.w3s;

import com.github.w3s.core.WebSocketMsgEndpoint;
import com.github.w3s.core.WssException;
import com.github.w3s.core.service.WebSocketAuthService;
import com.github.w3s.core.service.WebSocketService;
import com.github.w3s.core.session.SessionEvent;
import com.github.w3s.core.session.SessionMetaData;
import com.github.w3s.core.session.WebSocketSessionRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * web socket 处理类
 *
 * @author wang xiao
 * date 2022/5/11
 */
@ConditionalOnProperty(prefix = "w3s", name = "opened", havingValue = "true")
public class WebSocketHandler extends TextWebSocketHandler implements WebSocketMsgEndpoint {


    /**
     * 内部是 webSocket session id 与 session 引用
     */
    private static final ConcurrentMap<String, SessionMetaData> INTERNAL_SESSION_MAP = new ConcurrentHashMap<>();
    /**
     * 外部是 session 引用id 与 webSocket session id
     */
    private static final ConcurrentMap<String, String> EXTERNAL_SESSION_MAP = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private Optional<WebSocketAuthService> webSocketAuthServer;


    private WebSocketService webSocketService;

    private W3sConf wssConf;


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        SessionMetaData sessionMd = INTERNAL_SESSION_MAP.get(session.getId());
        if (sessionMd != null) {
            processSessionEvent(sessionMd.getSessionRef(), SessionEvent.onError(new WssException(exception)));
        }
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        try {
            if (session instanceof NativeWebSocketSession) {
                Session nativeSession = ((NativeWebSocketSession) session).getNativeSession(Session.class);
                if (nativeSession != null) {
                    nativeSession.getAsyncRemote().setSendTimeout(wssConf.getSendTimeOut());
                }
            }
            String internalSessionId = session.getId();
            WebSocketSessionRef sessionRef = toRef(session);
            String externalSessionId = sessionRef.getSessionId();
            boolean authRes = webSocketAuthServer.map(e -> e.checkLimits(internalSessionId, sessionRef.getUserId())).orElse(false);
            if (!authRes) {
                return;
            }
            INTERNAL_SESSION_MAP.put(internalSessionId, new SessionMetaData(session, sessionRef, this, 1000));
            EXTERNAL_SESSION_MAP.put(externalSessionId, internalSessionId);
            processSessionEvent(sessionRef, SessionEvent.onEstablished());
            logger.info(" Session :{} is opened,user id:{} ,external session id:{}", internalSessionId, sessionRef.getUserId(), externalSessionId);
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
                webSocketService.handleWebSocketMsg(sessionMd.getSessionRef(), message.getPayload());
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
                sessionMd.getSession().close(withReason);
            }
        }
    }

    private WebSocketSessionRef toRef(WebSocketSession session) {
        URI sessionUri = session.getUri();
        assert sessionUri != null;
        String path = sessionUri.getPath();
        String uriPath = path.substring(wssConf.getWsUrlPrefix().length());
        if (path.length() == 0) {
            throw new IllegalArgumentException("URL should contain plugin token!");
        }
        Long userId = webSocketAuthServer.map(e -> e.authAndReturnUserId(uriPath)).orElse(0L);
        ;
        return new WebSocketSessionRef(UUID.randomUUID().toString(), userId, session.getLocalAddress(), session.getRemoteAddress());
    }


    private void processSessionEvent(WebSocketSessionRef sessionRef, SessionEvent event) {
        try {
            webSocketService.handleWebSocketSessionEvent(sessionRef, event);
        } catch (BeanCreationNotAllowedException e) {
            logger.warn("[{}] Failed to close session due to possible shutdown state", sessionRef.getSessionId());
        }
    }

    @Autowired
    public void setWebSocketAuthServer(Optional<WebSocketAuthService> webSocketAuthServer) {
        this.webSocketAuthServer = webSocketAuthServer;
    }

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Autowired
    public void setWssConf(W3sConf wssConf) {
        this.wssConf = wssConf;
    }
}
