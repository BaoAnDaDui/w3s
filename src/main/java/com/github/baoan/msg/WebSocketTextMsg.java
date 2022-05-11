package com.github.baoan.msg;

import com.github.baoan.data.WebSocketMsgType;

/**
 * 报文消息
 * @author wang xiao
 * @date 2022/5/11
 */
public class WebSocketTextMsg implements WebSocketMsg<String>{

    private final String msg;

    public WebSocketTextMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public WebSocketMsgType getMsgType() {
        return WebSocketMsgType.TEXT;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
