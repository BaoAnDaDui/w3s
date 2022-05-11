package com.github.baoan.msg;

import com.github.baoan.data.WebSocketMsgType;

import java.nio.ByteBuffer;

/**
 * ping 消息
 * @author wang xiao
 * @date 2022/5/11
 */
public class WebSocketPingMsg implements WebSocketMsg<ByteBuffer> {

    public static WebSocketPingMsg INSTANCE = new WebSocketPingMsg();
    private static final ByteBuffer PING_MSG = ByteBuffer.wrap(new byte[]{});

    @Override
    public WebSocketMsgType getMsgType() {
        return WebSocketMsgType.PING;
    }

    @Override
    public ByteBuffer getMsg() {
        return PING_MSG;
    }
}
