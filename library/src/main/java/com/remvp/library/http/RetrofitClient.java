package com.remvp.library.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.remvp.library.bean.BaseResponse;
import com.remvp.library.http.cache.DiskLruCacheHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static io.reactivex.Observable.create;

/**
 * RetrofitClient基类
 */
public class RetrofitClient {
    private static final String TAG = "http RetrofitClient";
    public static final int DEFAULT_MILLISECONDS = 60000;             //默认的超时时间
    private OkHttpClient.Builder okHttpClientBuilder;                 //okhttp请求的客户端
    private Retrofit.Builder retrofitBuilder;                         //Retrofit请求Builder
    protected Retrofit retrofit;
    protected ApiService apiService;
    /**
     * 先加载缓存，缓存没有再去请求网络
     */
    public static final int CACHE_MODE_FIRST_CACHE = 1;
    /**
     * 先使用缓存，不管是否存在，仍然请求网络，会回调两次
     */
    public static final int CACHE_MODE_CACHE_AND_REMOTE = 2;

    public RetrofitClient() {
        init();
    }

    private void init() {
        okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        retrofitBuilder = new Retrofit.Builder();
    }

    /**
     * 多个域名
     *
     * @return
     */
    public RetrofitClient multiDomain() {
        okHttpClientBuilder = RetrofitUrlManager.getInstance().with(okHttpClientBuilder);
        return this;
    }

    /**
     * 缓存地址初始化
     *
     * @param context
     * @return
     */
    public RetrofitClient initCacheFile(Context context) {
        File httpCacheDirectory = new File(context.getExternalCacheDir(), "responses");
        DiskLruCacheHelper.getInstance().init(httpCacheDirectory, 10 * 1024 * 1024);
        return this;
    }

    /**
     * 添加全局拦截器
     *
     * @param interceptor 拦截器
     */
    public RetrofitClient addInterceptor(Interceptor interceptor) {
        okHttpClientBuilder.addInterceptor(interceptor);
        return this;
    }

    /**
     * 全局设置Converter.Factory,默认GsonConverterFactory.create()
     *
     * @param factory 解析Factory
     */
    public RetrofitClient addConverterFactory(Converter.Factory factory) {
        retrofitBuilder.addConverterFactory(factory);
        return this;
    }

    /**
     * 全局设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
     *
     * @param factory adapter Factory
     */
    public RetrofitClient addCallAdapterFactory(CallAdapter.Factory factory) {
        retrofitBuilder.addCallAdapterFactory(factory);
        return this;
    }

    /**
     * 全局设置baseUrl
     *
     * @param baseUrl 基地址
     */
    public RetrofitClient setBaseUrl(String baseUrl) {
        retrofitBuilder.baseUrl(baseUrl);
        setGlobalDomain(baseUrl);
        return this;
    }

    /**
     * 全局动态替换 BaseUrl，优先级： Header中配置的url > 全局配置的url
     * 除了作为备用的 BaseUrl ,当你项目中只有一个 BaseUrl ,但需要动态改变
     * 这种方式不用在每个接口方法上加 Header,也是个很好的选择
     *
     * @param url 全局域名
     */
    public void setGlobalDomain(String url) {
        RetrofitUrlManager.getInstance().setGlobalDomain(url);
    }

    /**
     * 存放 Domain 的映射关系
     *
     * @param domainName 域名key
     * @param domainUrl  域名
     */
    public RetrofitClient putDomain(String domainName, String domainUrl) {
        RetrofitUrlManager.getInstance().putDomain(domainName, domainUrl);
        return this;
    }

    public RetrofitClient build() {
        retrofitBuilder.client(okHttpClientBuilder.build());
        retrofit = retrofitBuilder.build();
        apiService = retrofit.create(ApiService.class);
        return this;
    }

    public ApiService getApiService() {
        return apiService;
    }

    /**
     * 获取数据，缓存或网络请求
     *
     * @param typeReference 转换类型
     * @param key           缓存key
     * @param cacheTime     缓存时间
     * @param mode          缓存模式
     * @param net           网络请求
     */
    protected <T extends BaseResponse> Observable<T> createCacheRequest(
            final TypeReference<T> typeReference,
            final String key,
            final long cacheTime,
            int mode,
            Observable<T> net) {
        if (mode != CACHE_MODE_CACHE_AND_REMOTE && mode != CACHE_MODE_FIRST_CACHE) {
            Log.i(TAG, "createCacheRequest: 不使用缓存，直接网络请求");
            return net;
        }
        Observable<T> cache = create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e) throws Exception {
                if (TextUtils.isEmpty(key)) {
                    e.onComplete();
                    return;
                }
                if (!DiskLruCacheHelper.getInstance().doContainsKey(key)) {
                    Log.i(TAG, "onSubscribe: 未缓存过数据，进行http请求");
                    e.onComplete();
                    return;
                }
                if (DiskLruCacheHelper.getInstance().isExpiry(key, cacheTime)) {
                    DiskLruCacheHelper.getInstance().remove(key);
                    Log.i(TAG, "onSubscribe: 缓存数据过期，进行http请求");
                    e.onComplete();
                    return;
                }
                String string = DiskLruCacheHelper.getInstance().getAsString(key);
                T bean = JSON.parseObject(string, typeReference.getType());
                if (bean.isSuccess()) {
                    Log.i(TAG, "subscribe: cache string " + string);
                    e.onNext(bean);
                }
                e.onComplete();
            }
        });
        if (mode == CACHE_MODE_FIRST_CACHE) {
            return Observable.concat(cache, net.doOnNext(new Consumer<T>() {
                @Override
                public void accept(T t) throws Exception {
                    Log.i(TAG, "apply: 保存数据");
                    DiskLruCacheHelper.getInstance().put(key, JSON.toJSONString(t));
                }
            })).firstElement().toObservable();
        } else {
            return Observable.concat(cache, net.doOnNext(new Consumer<T>() {
                @Override
                public void accept(T t) throws Exception {
                    Log.i(TAG, "apply: 保存数据");
                    DiskLruCacheHelper.getInstance().put(key, JSON.toJSONString(t));
                }
            }));
        }
    }

    /**
     * 将返回的String数据转换实体T
     *
     * @param typeReference 转换类型
     * @param <T>
     * @return
     */
    protected <T extends BaseResponse> ObservableTransformer<String, T> handleResult(
            final TypeReference<T> typeReference) {
        return new ObservableTransformer<String, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull final Observable<String> upstream) {
                return upstream.flatMap(new Function<String, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull String string) throws Exception {
                        Log.i(TAG, "apply: String转实体：" + string);
                        try {
                            T bean = JSON.parseObject(JSON.toJSONString(parseResult(string)),
                                    typeReference.getType());
                            if (bean.isSuccess()) {
                                return createData((T)
                                        JSON.parseObject(string, typeReference.getType()));
                            } else {
                                return Observable.error
                                        (new ServerException(bean.getMsg_code(), bean.getMsg()));
                            }
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                }).onErrorResumeNext(new ErrorResponseFunc<T>());
            }
        };
    }

    /**
     * 将返回的String数据转换实体T
     *
     * @param typeReference 转换类型
     * @param <T>
     * @return
     */
    protected <T extends BaseResponse> ObservableTransformer<String, T> handleCommonResult(
            final TypeReference<T> typeReference) {
        return new ObservableTransformer<String, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull final Observable<String> upstream) {
                return upstream.flatMap(new Function<String, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull String string) throws Exception {
                        Log.i(TAG, "apply: String转实体：" + string);
                        try {
                            T bean = JSON.parseObject(string, typeReference.getType());
                            if (bean.isSuccess()) {
                                return createData(bean);
                            } else {
                                return Observable.error
                                        (new ServerException(bean.getMsg_code(), bean.getMsg()));
                            }
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                }).onErrorResumeNext(new ErrorResponseFunc<T>());
            }
        };
    }

    /**
     * 创建返回data的ObservableSource
     *
     * @param data 数据
     * @param <T>
     * @return
     */
    protected <T> ObservableSource<T> createData(final T data) {
        return create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e) throws Exception {
                e.onNext(data);
                e.onComplete();
            }
        });
    }

    /**
     * 解析网络返回数据
     *
     * @param json 网络返回数据
     * @return
     * @throws JSONException
     */
    protected BaseResponse parseResult(String json) throws JSONException {
        if (TextUtils.isEmpty(json))
            return null;
        BaseResponse apiResult = new BaseResponse();
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("msg_code")) {
            apiResult.setMsg_code(jsonObject.getString("msg_code"));
        }
        if (jsonObject.has("msg")) {
            apiResult.setMsg(jsonObject.getString("msg"));
        }
        Log.i(TAG, "parseResult: " + apiResult.toString());
        return apiResult;
    }

    /**
     * 上传图片
     *
     * @param url        url地址，不带baseUrl,前面不用加"/"
     * @param bean       上传参数bean，转化为Json字符串，key为data
     * @param bytes      文件bytes[]
     * @param subscriber 处理返回数据
     * @param <T>
     * @param <R>
     */
    public <T, R extends BaseResponse> void postFile(
            String url,
            T bean,
            byte[] bytes,
            BaseObserver<R> subscriber) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", "name", new FormBody.Builder()
                        .add("data", JSON.toJSONString(bean))
                        .build())
                .addFormDataPart("file_base64", "file_avatar.jpg", requestFile);
        List<MultipartBody.Part> parts = builder.build().parts();
        apiService.postBodyFile(url, parts)
                .compose(handleResult(subscriber.getTypeReference()))
                .compose(SchedulersTransformer.io_main())
                .subscribe(subscriber);
    }

    /**
     * 带缓存的通用post请求
     *
     * @param domain     域名名称
     * @param url        url地址，不带baseUrl,前面不用加"/"
     * @param body       上传参数
     * @param time       缓存时间
     * @param mode       缓存模式
     * @param subscriber 处理返回数据
     * @param <R>
     */
    public <R extends BaseResponse> void realPost(
            String domain,
            String url,
            RequestBody body,
            String key,
            long time,
            int mode,
            BaseObserver<R> subscriber) {
        Observable<R> formNet;
        if (domain == null) {
            formNet = apiService.
                    postBody(url, body)
                    .compose(handleResult(subscriber.getTypeReference()));
        } else {
            formNet = apiService.
                    postBody(domain, url, body)
                    .compose(handleResult(subscriber.getTypeReference()));
        }

        createCacheRequest(subscriber.getTypeReference(),
                key, time, mode, formNet)
                .compose(SchedulersTransformer.io_main())
                .subscribe(subscriber);
    }
}
