package com.github.wss.core.data;

/**
 * web socket 常量
 * @author wang xiao
 * date 2022/5/11
 */
public class WebSocketConstant {


    public WebSocketConstant() {
    }

    public static final int NUMBER_OF_PING_ATTEMPTS = 3;

    public static final long SEND_TIMEOUT = 5000;

    public static final long PING_TIMEOUT = 30000;


    public static final String WS_PLUGIN_PREFIX = "/ws";


    public static final Integer MAX_SUB_OF_SESSION = 20;



}
