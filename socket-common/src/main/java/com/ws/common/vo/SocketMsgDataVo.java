package com.ws.common.vo;

public class SocketMsgDataVo {

    private byte type;  // 0 - 心跳检测； 1 内容传输
    private String body; //正文内容

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "SocketMsgDataVo{" +
                "type=" + type +
                ", body='" + body + '\'' +
                '}';
    }
}
