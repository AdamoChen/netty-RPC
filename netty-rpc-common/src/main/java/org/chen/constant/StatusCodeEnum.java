package org.chen.constant;

public enum  StatusCodeEnum {
    SUCCESS(1, "成功"),
    FAILURE(-1, "失败"),
    ;

    public int code;
    public String desc;

    StatusCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }



}
