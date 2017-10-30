package com.remvp.library.http;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

import static com.remvp.library.http.RetrofitUrlManager.DOMAIN_NAME;

/**
 * <p>描述：通用的的api接口</p>
 * <p>
 * <p>
 * 1.加入基础API，减少Api冗余<br>
 * 2.支持多种方式访问网络（get,put,post,delete），包含了常用的情况<br>
 * 3.传统的Retrofit用法，服务器每增加一个接口，就要对应一个api，非常繁琐<br>
 * 4.如果返回ResponseBody在返回的结果中去获取T,又会报错，这是因为在运行过程中,通过泛型传入的类型T丢失了,所以无法转换,这叫做泛型擦除。
 * 《泛型擦除》不知道的童鞋自己百度哦！！<br>
 * </p>
 * <p>
 * 注意事项：<br>
 * 1.使用@url,而不是@Path注解,后者放到方法体上,会强制先urlencode,然后与baseurl拼接,请求无法成功<br>
 * 2.注意事项： map不能为null,否则该请求不会执行,但可以size为空.<br>
 * 3.不能使用泛型，在运行过程中,通过泛型传入的类型T丢失了,所以无法转换,这叫做泛型擦除<br>
 * </p>
 */
public interface ApiService {

    @DELETE("{url}")
    Observable<ResponseBody> delete(@Path("url") String url, @QueryMap Map<String, String> maps);

    @PUT("{url}")
    Observable<ResponseBody> put(@Path("url") String url, @QueryMap Map<String, String> maps);

    /**
     * 切换域名的get请求
     *
     * @param name 域名
     * @param url  地址
     * @param body 请求参数
     * @return
     */
    @GET()
    Observable<String> get(
            @Header(DOMAIN_NAME) String name,
            @Url() String url,
            @Body RequestBody body);

    /**
     * 普通的post请求
     *
     * @param url  地址
     * @param body 请求参数
     * @return
     */
    @POST()
    Observable<String> postBody(
            @Url() String url,
            @Body RequestBody body);

    /**
     * 切换域名的post请求
     *
     * @param name 域名
     * @param url  地址
     * @param body 请求参数
     * @return
     */
    @POST()
    Observable<String> postBody(
            @Header(DOMAIN_NAME) String name,
            @Url() String url,
            @Body RequestBody body);


    /**
     * 上传图片或文件
     *
     * @param url   地址
     * @param parts 请求参数
     * @return
     */
    @Multipart
    @POST()
    Observable<String> postBodyFile(
            @Url() String url,
            @Part List<MultipartBody.Part> parts);

    /**
     * 切换域名的上传图片或文件
     *
     * @param name  域名
     * @param url   地址
     * @param parts 请求参数
     * @return
     */
    @Multipart
    @POST()
    Observable<String> postBodyFile(
            @Header(DOMAIN_NAME) String name,
            @Url() String url,
            @Part List<MultipartBody.Part> parts);

    /**
     * xml请求
     * 需添加依赖
     * compile ('com.squareup.retrofit2:converter-simplexml:2.3.0'){
     * exclude group: 'xpp3', module: 'xpp3'
     * exclude group: 'stax', module: 'stax-api'
     * exclude group: 'stax', module: 'stax'
     * }
     *
     * @param name 域名名称
     * @param url  url地址
     * @param body 参数RequestBody.create(MediaType.parse("text/xml;charset=ISO8859-1"), string)
     * @return
     */
    @POST()
    @MultiConverters.Xml
    Observable<ResponseBody> postXml(
            @Header(RetrofitUrlManager.DOMAIN_NAME) String name,
            @Url() String url,
            @Body RequestBody body);

}
