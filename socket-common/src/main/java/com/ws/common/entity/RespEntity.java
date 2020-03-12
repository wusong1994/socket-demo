package com.ws.common.entity;

/**
 * 响应信息
 */
public class RespEntity {

    private int code; //状态码
    private String data; //响应结果

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
