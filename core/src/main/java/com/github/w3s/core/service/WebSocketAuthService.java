package com.github.w3s.core.service;

/**
 * @author wang xiao
 * date 2022/5/11
 */
public interface WebSocketAuthService {

    /**
     * 授权 web socket 地址 并返回用户id 抛出异常为失败
     *
     * @param path web socket 地址
     * @return 用户id
     */
    Long authAndReturnUserId(String path);


    /**
     * 判断 是否超出 用户限制数
     *
     * @param sessionId session id
     * @param userId    user id
     * @return boolean
     */
    boolean checkLimits(String sessionId, Long userId);
}
