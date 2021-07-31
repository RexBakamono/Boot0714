package com.rex.common.code;

import java.util.Objects;

public class ResCode {
    public static ResCode SUCCESSED = new ResCode(200, "操作成功");
    public static ResCode FAIL = new ResCode(500, "操作失败");

    private Integer code;
    private String msg;

    public ResCode(int code, String msg) {
        this.code = Objects.requireNonNull(code);
        this.msg = Objects.requireNonNull(msg);
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }


}
