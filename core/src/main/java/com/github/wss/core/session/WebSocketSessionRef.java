package com.github.wss.core.session;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * web socket session 的引用
 *
 * @author wang xiao
 * date 2022/5/11
 */
public class WebSocketSessionRef {

    private final String sessionId;

    private final Long userId;

    private final InetSocketAddress localAddress;

    private final InetSocketAddress remoteAddress;

    private final AtomicInteger sessionSubIdSeq;


    public WebSocketSessionRef(String sessionId, Long userId, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.sessionSubIdSeq = new AtomicInteger(0);
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public AtomicInteger getSessionSubIdSeq() {
        return sessionSubIdSeq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebSocketSessionRef that = (WebSocketSessionRef) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sessionId);
    }

    @Override
    public String toString() {
        return "WebSocketSessionRef{" +
                "sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", localAddress=" + localAddress +
                ", remoteAddress=" + remoteAddress +
                ", sessionSubIdSeq=" + sessionSubIdSeq +
                '}';
    }
}
