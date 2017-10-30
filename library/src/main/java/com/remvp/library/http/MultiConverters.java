package com.remvp.library.http;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 多种转换器
 * 默认为Json
 */
public final class MultiConverters {
    @Retention(RUNTIME)
    public @interface Json {
    }

    @Retention(RUNTIME)
    public @interface Xml {
    }

    /**
     * 请求使用Json响应用Xml
     */
    @Retention(RUNTIME)
    public @interface ReqJsonRespXml {
    }

    /**
     * 请求使用Xml响应用Json
     */
    @Retention(RUNTIME)
    public @interface ReqXmlRespJson {
    }

    public static class MultiConverterFactory extends Converter.Factory {
        private final Converter.Factory jsonFactory;
        private final Converter.Factory xmlFactory;

        /**
         * @param jsonFactory json转换器
         * @param xmlFactory  xml转换器
         */
        public MultiConverterFactory(Converter.Factory jsonFactory, Converter.Factory xmlFactory) {
            this.jsonFactory = jsonFactory;
            this.xmlFactory = xmlFactory;
        }

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                                Retrofit retrofit) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Xml || annotation instanceof ReqJsonRespXml) {
                    return xmlFactory.responseBodyConverter(type, annotations, retrofit);
                }
            }
            return jsonFactory.responseBodyConverter(type, annotations, retrofit);
        }

        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof Xml || annotation instanceof ReqXmlRespJson) {
                    return xmlFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations,
                            retrofit);
                }
            }
            return jsonFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations,
                    retrofit);
        }
    }
}
