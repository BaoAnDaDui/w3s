package com.github.baoan.data;

import java.util.Optional;

/**
 * session event
 * @author wang xiao
 * @date 2022/5/11
 */
public class SessionEvent {

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
    };


    private final SessionEventType eventType;

    private final Optional<Throwable> error;

    private SessionEvent(SessionEventType eventType, Throwable error) {
        super();
        this.eventType = eventType;
        this.error = Optional.ofNullable(error);
    }

    public SessionEventType getEventType() {
        return eventType;
    }

    public Optional<Throwable> getError() {
        return error;
    }

    public static SessionEvent onEstablished() {
        return new SessionEvent(SessionEventType.ESTABLISHED, null);
    }

    public static SessionEvent onClosed() {
        return new SessionEvent(SessionEventType.CLOSED, null);
    }

    public static SessionEvent onError(Throwable t) {
        return new SessionEvent(SessionEventType.ERROR, t);
    }
}
