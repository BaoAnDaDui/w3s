package com.github.w3s.example.client;

import javax.websocket.*;

/**
 * @author wang xiao
 * date 2022/10/26
 */
@ClientEndpoint()
public class WsClientHandler {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("session connect" + session);
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println(message);
    }

    @OnClose
    public void onClose(Session session) {

    }

}
