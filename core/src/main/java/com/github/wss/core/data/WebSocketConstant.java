package com.github.wss.core.data;

/**
 * web socket 常量
 *
 * @author wang xiao
 * date 2022/5/11
 */
public interface WebSocketConstant {

    int NUMBER_OF_PING_ATTEMPTS = 3;

    long SEND_TIMEOUT = 5000;

    long PING_TIMEOUT = 30000;


    String WS_PLUGIN_PREFIX = "/ws";


    Integer MAX_SUB_OF_SESSION = 20;

}
