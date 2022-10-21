package com.github.wss.core.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author wang xiao
 * date 2022/10/2
 */
@Service
public class DefaultWebSocketAuthServer implements WebSocketAuthService {

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public Long authAndReturnUserId(String path) {
        return random.nextLong();
    }

    @Override
    public boolean checkLimits(String sessionId, Long userId) {
        return true;
    }
}
