package com.rex.common.code;

import java.util.Objects;

public class ApiMessage<T> {

    private int code;

    private String message;

    private T data;

    public ApiMessage() {
        this(ResCode.SUCCESSED);
    }

    public ApiMessage(T data) {
        this(ResCode.SUCCESSED, data);
    }

    public ApiMessage(ResCode errCode) {
        this(errCode, null);
    }

    public ApiMessage(ResCode errCode, T data) {
        Objects.requireNonNull(errCode);
        this.code = errCode.getCode();
        this.message = errCode.getMsg();
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
