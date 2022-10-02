package com.github.baoan.server;

import org.springframework.stereotype.Service;

/**
 * @author wang xiao
 * @date 2022/10/2
 */
@Service
public class DefaultWebSocketAuthServer implements WebSocketAuthServer{

    @Override
    public Long authAndReturnUserId(String path) {
        return null;
    }

    @Override
    public boolean checkLimits(String sessionId, Long userId) {
        return false;
    }
}
