# ReMvp
### 网络请求封装 Retrofit2.0 + RxJava + OkHttp
1. 网络请求使用 [RetrofitClient](https://github.com/ElaineJinooo/ReMvp/blob/master/library/src/main/java/com/remvp/library/http/RetrofitClient.java)
 - 支持多个域名
 - 支持多个解析转换器 [MultiConverters](https://github.com/ElaineJinooo/ReMvp/blob/master/library/src/main/java/com/remvp/library/http/MultiConverters.java)
 - 可添加公共参数、签名动态拦截器 [BaseDynamicInterceptor](https://github.com/ElaineJinooo/ReMvp/blob/master/library/src/main/java/com/remvp/library/http/BaseDynamicInterceptor.java)
2. 数据库继承 [DBHelper](https://github.com/ElaineJinooo/ReMvp/blob/master/library/src/main/java/com/remvp/library/db/orm/DBHelper.java)
3. WebView 使用 [WebViewUtil](https://github.com/ElaineJinooo/ReMvp/blob/master/library/src/main/java/com/remvp/library/util/web/WebViewUtil.java)