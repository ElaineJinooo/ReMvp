package com.remvp.library.bean;

import com.remvp.library.http.cache.ResSuccess;

import java.io.Serializable;

public class BaseResponse<T> implements Serializable, ResSuccess {
    public String msg_code = ""; // 描述信息
    public String msg = ""; // 结果码
    public String server_time = "";
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg_code() {
        return msg_code;
    }

    public void setMsg_code(String msg_code) {
        this.msg_code = msg_code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getServer_time() {
        return server_time;
    }

    public void setServer_time(String server_time) {
        this.server_time = server_time;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "msg_code='" + msg_code + '\'' +
                ", msg='" + msg + '\'' +
                ", server_time='" + server_time + '\'' +
                ", data=" + data +
                '}';
    }
}
