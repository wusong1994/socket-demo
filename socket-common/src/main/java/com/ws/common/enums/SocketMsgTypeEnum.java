package com.ws.common.enums;

/**
 * socket 发送消息类型
 */
public enum SocketMsgTypeEnum {

    SERVER_NOT_ALLOW((byte) -1, "仅允许一个客户端保持长连接，新的将关闭"),
    HEART_BEAT((byte) 0, "心跳消息"),
    REALTIME_PUSH_NOTIFY((byte) 1, "即时推送通知");

    private byte type;
    private String desc;

    SocketMsgTypeEnum(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
