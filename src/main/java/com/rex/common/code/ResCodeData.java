package com.rex.common.code;

public class ResCodeData extends ResCode {

    public static ResCodeData LOGIN_ERROR = new ResCodeData(-101, "账号或密码错误");

    public ResCodeData(int code, String msg) {
        super(code, msg);
    }

}
