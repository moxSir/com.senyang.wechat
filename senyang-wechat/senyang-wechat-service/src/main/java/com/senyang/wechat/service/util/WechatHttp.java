package com.senyang.wechat.service.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.senyang.common.web.BaseResponse;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.apache.commons.lang3.tuple.Pair;

public class WechatHttp {
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy
            .LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
	
	public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(16, 300, 2, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(500), new CallerRunsPolicy());

    public static final ThreadPoolExecutor EXECUTOR_BIZ = new ThreadPoolExecutor(20, 3000, 30, TimeUnit.MINUTES, new
            ArrayBlockingQueue<Runnable>(500), new RejectedExecutionHandler() {


        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
            try {
                final Thread t = new Thread(r, "Temporary task executor");
                t.start();
            } catch (Throwable e) {
                throw new RejectedExecutionException("Failed to start a new thread", e);
            }
        }
    });

    private static final Logger LOGGER = LoggerFactory.getLogger(WechatHttp.class);

    private static final Interceptor INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(final Chain chain) throws IOException {
            Pair<Response, String> pair = $process(chain);
            Response response = pair.getKey();
            String msg = pair.getValue();
            BaseResponse responseJson = GSON.fromJson(msg, BaseResponse.class);

            //code=561, message=FastReject
            //{errcode=45011, errmsg='api freq reach limit, must slower'}
            if (response.code() == 561 || responseJson.errcode == 45011) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(3));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pair = $process(chain);
                response = pair.getKey();
                msg = pair.getValue();
                responseJson = GSON.fromJson(msg, BaseResponse.class);
            }

            if (responseJson.errcode != 0) {
                throw new IOException("invalid wechat response:" + responseJson);
            }

            return new Response.Builder()
                    .headers(response.headers())
                    .code(response.code())
                    .message(response.message())
                    .protocol(response.protocol())
                    .request(chain.request())
                    .body(ResponseBody.create(response.body()
                            .contentType(), msg))
                    .build();
        }

        Pair<Response, String> $process(Chain chain) throws IOException {
            final long startTime = System.currentTimeMillis();
            LOGGER.info("request:{}", chain.request());
            final Response response = chain.proceed(chain.request());
            final String msg = response.body()
                    .string();
            LOGGER.info(" response: code={}, message={}, time:{}, text={}", response.code() + "", response.message(),
                    System.currentTimeMillis() - startTime, msg);
            return Pair.of(response, msg);
        }
    };

    public static WechatToken wechatToken = WechatToken.INSTANCE;

    static {
        EXECUTOR.prestartAllCoreThreads();
        EXECUTOR.prestartCoreThread();

        EXECUTOR_BIZ.prestartAllCoreThreads();
        EXECUTOR_BIZ.prestartCoreThread();
    }

    public static <T> T get(final String api, final Map<String, String> param, final Class<T> clz) throws IOException {
        param.put("access_token", getAccessToken());
        OkHttpClient client = new OkHttpClient();
        client.interceptors()
                .add(INTERCEPTOR);
        final String url = "https://api.weixin.qq.com/cgi-bin/" + api + "?" + Joiner.on('&')
                .withKeyValueSeparator("=")
                .useForNull("")
                .join(param);
        
        
        final String response = client.newCall(new Request.Builder().url(url)
                .get()
                .build())
                .execute()
                .body()
                .string();
        
        
        return GSON.fromJson(response, clz);

    }

    public static <T> T post(final String api, final Object param, final Class<T> clz) throws IOException {
        OkHttpClient client = new OkHttpClient();
        client.interceptors()
                .add(INTERCEPTOR);
        final String url = "https://api.weixin.qq.com/cgi-bin/" + api + "?access_token=" + getAccessToken();
        final String postString = GSON.toJson(param);
        
        
        final String response = client.newCall(new Request.Builder().url(url)
                .post(RequestBody.create(MediaType.parse(com.google.common.net.MediaType.PLAIN_TEXT_UTF_8.toString())
                        , postString))
                .build())
                .execute()
                .body()
                .string();
        
        
        return GSON.fromJson(response, clz);
    }

    public static <T> T get2(final String api, final Map<String, String> param, final Class<T> clz) throws IOException {
        param.put("access_token", getAccessToken());
        OkHttpClient client = new OkHttpClient();
        client.interceptors()
                .add(INTERCEPTOR);
        final String url = "https://api.weixin.qq.com/" + api + "?" + Joiner.on('&')
                .withKeyValueSeparator("=")
                .useForNull("")
                .join(param);
        
        
        final String response = client.newCall(new Request.Builder().url(url)
                .get()
                .build())
                .execute()
                .body()
                .string();
        
        
        return GSON.fromJson(response, clz);

    }

    public static <T> T post2(final String api, final Object param, final Class<T> clz) throws IOException {
        OkHttpClient client = new OkHttpClient();
        client.interceptors()
                .add(INTERCEPTOR);
        final String url = "https://api.weixin.qq.com/" + api + "?access_token=" + getAccessToken();
        final String postString = GSON.toJson(param);
        LOGGER.info("post:{}", postString);
        
        
        final String response  = client.newCall(new Request.Builder().url(url)
                .post(RequestBody.create(MediaType.parse(com.google.common.net.MediaType.PLAIN_TEXT_UTF_8.toString()), postString))
                .build())
                .execute()
                .body()
                .string();
    	
        return GSON.fromJson(response, clz);
    }

    private static String getAccessToken() {
        return wechatToken.getAccessToken();
    }
    
    public static String get(String url) throws IOException{
    	OkHttpClient client = new OkHttpClient();
    	client.interceptors().add(INTERCEPTOR);
    	client.setReadTimeout(3000, TimeUnit.SECONDS);
    	client.setWriteTimeout(3000, TimeUnit.SECONDS);
    	
    	
		String response  = client.newCall(new Request.Builder().url(url)
                .post(RequestBody.create(MediaType.parse(com.google.common.net.MediaType.PLAIN_TEXT_UTF_8.toString()), ""))
                .build())
                .execute()
                .body()
                .string();
		
		return  response;
    }
}
