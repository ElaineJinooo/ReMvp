package com.remvp.library.http;

import android.util.Log;

import org.json.JSONException;

import java.io.NotSerializableException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

/**
 * 异常
 */
public class ApiException extends Exception {
    private static final String TAG = "ApiException";
    private final String code;
    private String displayMessage;
    private String message;

    public ApiException(Throwable throwable, String code) {
        super(throwable);
        this.code = code;
        this.message = throwable.getMessage();
    }

    public String getCode() {
        return code;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String msg) {
        this.displayMessage = msg + "(code:" + code + ")";
    }

    public static ApiException handleException(Throwable e) {
        Log.i(TAG, "http handleException: " + e.toString());
        ApiException ex;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ApiException(httpException, httpException.code() + "");
            ex.message = httpException.getMessage();
            return ex;
        } else if (e instanceof ServerException) {
            ServerException resultException = (ServerException) e;
            ex = new ApiException(resultException, resultException.getErrCode());
            ex.message = resultException.getMessage();
            return ex;
        } else if (e instanceof com.alibaba.fastjson.JSONException
                || e instanceof JSONException
//                || e instanceof JsonSyntaxException
//                || e instanceof JsonSerializer
                || e instanceof NotSerializableException
//                || e instanceof ParseException
                ) {
            ex = new ApiException(e, ERROR.PARSE_ERROR);
            ex.message = "服务器数据返回错误";
            return ex;
        } else if (e instanceof ClassCastException) {
            ex = new ApiException(e, ERROR.CAST_ERROR);
            ex.message = "类型转换错误";
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new ApiException(e, ERROR.NETWORD_ERROR);
            ex.message = "网络走神了，要不再试试吧~";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ApiException(e, ERROR.SSL_ERROR);
            ex.message = "证书验证失败";
            return ex;
        } else if (e instanceof TimeoutException) {
            ex = new ApiException(e, ERROR.TIMEOUT_ERROR);
            ex.message = "网络连接超时";
            return ex;
        } else if (e instanceof java.net.SocketTimeoutException) {
            ex = new ApiException(e, ERROR.TIMEOUT_ERROR);
            ex.message = "网络连接超时";
            return ex;
        } else if (e instanceof UnknownHostException) {
            ex = new ApiException(e, ERROR.UNKNOWNHOST_ERROR);
//            ex.message = "无法解析该域名";
            ex.message = "网络走神了，要不再试试吧~";
            return ex;
        } else {
            ex = new ApiException(e, ERROR.UNKNOWN);
            ex.message = "未知错误";
            return ex;
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    /*public String getErrMessage() {
        return message;
    }*/

    /**
     * 约定异常
     */
    public static class ERROR {
        /**
         * 未知错误
         */
        public static final String UNKNOWN = "10000";
        /**
         * 解析错误
         */
        public static final String PARSE_ERROR = "10001";
        /**
         * 网络错误
         */
        public static final String NETWORD_ERROR = "10002";
        /**
         * 协议出错
         */
        public static final String HTTP_ERROR = "10003";

        /**
         * 证书出错
         */
        public static final String SSL_ERROR = "10004";

        /**
         * 连接超时
         */
        public static final String TIMEOUT_ERROR = "10005";

        /**
         * 调用错误
         */
        public static final String INVOKE_ERROR = "10006";
        /**
         * 类转换错误
         */
        public static final String CAST_ERROR = "10007";
        /**
         * 请求取消
         */
        public static final String REQUEST_CANCEL = "10008";
        /**
         * 未知主机错误
         */
        public static final String UNKNOWNHOST_ERROR = "10009";
        /**
         * 拦截错误
         */
        public static final String CATCH_ERROR = "-10000";
    }
}
