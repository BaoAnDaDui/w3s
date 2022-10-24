package com.github.wss.core.session;

import com.github.wss.core.WssException;

import java.util.Optional;

/**
 * session event
 *
 * @author wang xiao
 * date 2022/5/11
 */
public class SessionEvent {

    private final SessionEventType eventType;
    private final WssException error;

    private SessionEvent(SessionEventType eventType, WssException error) {
        super();
        this.eventType = eventType;
        this.error = error;
    }

    public static SessionEvent onEstablished() {
        return new SessionEvent(SessionEventType.ESTABLISHED, null);
    }

    public static SessionEvent onClosed() {
        return new SessionEvent(SessionEventType.CLOSED, null);
    }

    public static SessionEvent onError(WssException t) {
        return new SessionEvent(SessionEventType.ERROR, t);
    }

    public SessionEventType getEventType() {
        return eventType;
    }

    public Optional<WssException> getError() {
        return Optional.ofNullable(error);
    }

    public enum SessionEventType {
        /**
         * 连接建立
         */
        ESTABLISHED,
        /**
         * 连接 关闭
         */
        CLOSED,
        /**
         * 连接 错误
         */
        ERROR
    }
}
