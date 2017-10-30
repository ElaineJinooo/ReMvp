package com.remvp.library.http.cache;

import com.remvp.library.bean.BaseResponse;

/**
 * 判断网络请求成功后{@link BaseResponse#getMsg_code()}数据是否成功
 * 与服务端协定的策略
 */
public interface ResSuccess {
    /**
     * 是否成功
     *
     * @return
     */
    boolean isSuccess();
}
