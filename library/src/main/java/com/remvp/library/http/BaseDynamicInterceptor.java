package com.remvp.library.http;

import android.util.Log;

import com.remvp.library.util.HttpUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 公共参数、签名动态拦截器基类
 */
public abstract class BaseDynamicInterceptor implements Interceptor {
    private static final String TAG = "BaseDynamicInterceptor";
    protected HttpUrl httpUrl;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Log.i(TAG, "intercept: " + request.toString());
        if (request.method().equals("GET")) {
            this.httpUrl = HttpUrl.parse(parseUrl(request.url().url().toString()));
            request = addGetParamsSign(request);
        } else if (request.method().equals("POST")) {
            this.httpUrl = request.url();
            request = addPostParamsSign(request);
        }
        return chain.proceed(request);
    }

    /**
     * get 添加签名和公共动态参数
     *
     * @param request 请求
     * @return
     * @throws UnsupportedEncodingException
     */
    private Request addGetParamsSign(Request request) throws UnsupportedEncodingException {
        HttpUrl httpUrl = request.url();
        HttpUrl.Builder newBuilder = httpUrl.newBuilder();

        //获取原有的参数
        Set<String> nameSet = httpUrl.queryParameterNames();
        ArrayList<String> nameList = new ArrayList<>();
        nameList.addAll(nameSet);
        TreeMap<String, String> oldparams = new TreeMap<>();
        for (int i = 0; i < nameList.size(); i++) {
            String value = httpUrl.queryParameterValues(nameList.get(i)) != null && httpUrl.queryParameterValues(nameList.get(i)).size() > 0 ? httpUrl.queryParameterValues(nameList.get(i)).get(0) : "";
            oldparams.put(nameList.get(i), value);
        }
        String nameKeys = Arrays.asList(nameList).toString();
        //拼装新的参数
        TreeMap<String, String> newParams = dynamic(oldparams);
        for (Map.Entry<String, String> entry : newParams.entrySet()) {
            String urlValue = URLEncoder.encode(entry.getValue(), Charset.forName("UTF-8").name());
            //原来的URl: https://xxx.xxx.xxx/app/chairdressing/skinAnalyzePower/skinTestResult?appId=10101
            if (!nameKeys.contains(entry.getKey())) {//避免重复添加
                newBuilder.addQueryParameter(entry.getKey(), urlValue);
            }
        }

        httpUrl = newBuilder.build();
        request = request.newBuilder().url(httpUrl).build();
        return request;
    }

    /**
     * realPost 添加签名和公共动态参数
     *
     * @param request 请求
     * @return
     * @throws UnsupportedEncodingException
     */
    private Request addPostParamsSign(Request request) throws UnsupportedEncodingException {
        if (request.body() instanceof FormBody) {
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            FormBody formBody = (FormBody) request.body();

            //原有的参数
            TreeMap<String, String> oldparams = new TreeMap<>();
            for (int i = 0; i < formBody.size(); i++) {
                oldparams.put(formBody.name(i), formBody.value(i));
            }

            //拼装新的参数
            TreeMap<String, String> newParams = dynamic(oldparams);

            for (Map.Entry<String, String> entry : newParams.entrySet()) {
                String value = URLEncoder.encode(entry.getValue(), Charset.forName("UTF-8").name());
                bodyBuilder.addEncoded(entry.getKey(), value);
            }
            HttpUtil.createUrlFromParams(httpUrl.url().toString(), newParams);

            formBody = bodyBuilder.build();
            request = request.newBuilder().post(formBody).build();
        } else if (request.body() instanceof MultipartBody) {
            MultipartBody multipartBody = (MultipartBody) request.body();
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            List<MultipartBody.Part> oldparts = multipartBody.parts();

            //拼装新的参数
            List<MultipartBody.Part> newparts = new ArrayList<>();

            TreeMap<String, String> oldparams = new TreeMap<>();
            for (MultipartBody.Part part : oldparts) {
                if (part.body() instanceof FormBody) {
                    FormBody formBody = (FormBody) part.body();
                    for (int i = 0; i < formBody.size(); i++) {
                        oldparams.put(formBody.name(i), formBody.value(i));
                    }
                } else {
                    newparts.add(part);
                }
            }

            TreeMap<String, String> newParams = dynamic(oldparams);

            for (Map.Entry<String, String> stringStringEntry : newParams.entrySet()) {
                MultipartBody.Part part = MultipartBody.Part.createFormData(stringStringEntry.getKey(), stringStringEntry.getValue());
                newparts.add(part);

            }
            for (MultipartBody.Part part : newparts) {
                bodyBuilder.addPart(part);
            }
            multipartBody = bodyBuilder.build();
            request = request.newBuilder().post(multipartBody).build();
        } else {
            Log.i(TAG, "addPostParamsSign: " + request.body().getClass().getName());
        }
        return request;
    }


    //解析前：https://xxx.xxx.xxx/app/chairdressing/skinAnalyzePower/skinTestResult?appId=10101
    //解析后：https://xxx.xxx.xxx/app/chairdressing/skinAnalyzePower/skinTestResult

    /**
     * 去掉url的参数
     *
     * @param url 待处理的url
     * @return
     */
    private String parseUrl(String url) {
        if (!"".equals(url) && url.contains("?")) {// 如果URL不是空字符串
            url = url.substring(0, url.indexOf('?'));
        }
        return url;
    }

    /**
     * 动态处理参数
     *
     * @param dynamicMap 原始参数
     * @return 返回新的参数集合
     */
    public abstract TreeMap<String, String> dynamic(TreeMap<String, String> dynamicMap);
}
