package com.adamo.service.dto;

public class Request{

    private String requestId;

    private String classFullName;

    private String methodName;

    private Class<?>[] argsType;

    private Object[] args;

    /**
     * 是否是心跳请求 默认为否
     */
    private boolean heartBeat;

    public boolean isHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(boolean heartBeat) {
        this.heartBeat = heartBeat;
    }

    public Class<?>[] getArgsType() {
        return argsType;
    }

    public void setArgsType(Class<?>[] argsType) {
        this.argsType = argsType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassFullName() {
        return classFullName;
    }

    public void setClassFullName(String classFullName) {
        this.classFullName = classFullName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
