package com.adamo.service.dto;

public class Response{

    private String requestId;

    private int code;

    private String errorMsg;

    private Object data;

    private boolean heartBeat;

    // note 必须保留 否则fastjson反序列化存在问题
    public Response() { }

    public Response(String requestId, boolean heartBeat) {
        this.requestId = requestId;
        this.heartBeat = heartBeat;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(boolean heartBeat) {
        this.heartBeat = heartBeat;
    }
}
