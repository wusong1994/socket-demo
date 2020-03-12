package com.ws.common.vo;

/**
 * 即时推送消息实体封装
 */
public class RealTimeMsgEntity {

    private String dataId; //记录主键ID
    private int interfaceId; //接口ID
    private String reqParam; //请求参数

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public int getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getReqParam() {
        return reqParam;
    }

    public void setReqParam(String reqParam) {
        this.reqParam = reqParam;
    }
}
