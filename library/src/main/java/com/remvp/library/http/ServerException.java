/*
 * Copyright (C) 2017 zhouyou(478319399@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.remvp.library.http;

/**
 * <p>描述：处理服务器异常</p>
 */
public class ServerException extends RuntimeException {
    private String errCode;
    private String message;

    public ServerException(String errCode, String msg) {
        super(msg);
        this.errCode = errCode;
        this.message = msg;
    }

    public String getErrCode() {
        return errCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}