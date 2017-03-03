package com.example.swaggerdemo.network;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.swaggerdemo.network.gson.NullStringToEmptyAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by james.li on 2017/3/3.
 */

public class HttpService {
    public static final String BASE_URL = "http://petstore.swagger.io/v2/";
    public static final int DEFAULT_TIMEOUT = 3000;
    private static final String TAG = "HttpService";
    public String token;
    public String userId;
    public String userName;
    public String nickName;
    private Retrofit retrofit;
    public HttpService() {
        //log打印
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(mTokenInterceptor)
                .authenticator(mAuthenticator)
                .addInterceptor(loggingInterceptor)
                .build();

        //json解析属性值为null转换为""
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory<>())
                .create();
        //第三种方式

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

    //统一添加token处理
    Interceptor mTokenInterceptor = new Interceptor() {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            //如果是登录之类的没有token
            if (TextUtils.isEmpty(token)) {
                return chain.proceed(originalRequest);
            }

            //如果请求是POST，应该是添加到表单(奇怪会拼接到url？)
            /*if (originalRequest.method().equals("POST")) {
                Log.d(TAG, "intercept: post method");
                Request authorised = originalRequest.newBuilder()
                        .addHeader("token", token)
                        .addHeader("userId", userId)
                        .build();
                return chain.proceed(authorised);
            }*/
            //如果请求是GET，应该添拼接到url
            else if (originalRequest.method().equals("GET")){
                //添加http get 添加token
                Log.d(TAG, "intercept: http get add token: " + token + "add userId: " + userId);
                HttpUrl httpUrl = originalRequest.url();
                HttpUrl url = httpUrl.newBuilder()
                        .addQueryParameter("token", token)
                        .addQueryParameter("userId", userId)
                        .addQueryParameter("nickName", nickName)
                        .build();

                Request.Builder requestBuilder = originalRequest.newBuilder()
                        .url(url);
                Log.w(TAG, "intercept: http url: " + url.toString() );
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
            return chain.proceed(originalRequest);
        }
    };

    //如果你需要在遇到诸如 401 Not Authorised 的时候进行刷新 token，可以使用 Authenticator，这是一个专门设计用于当验证出现错误的时候，进行询问获取处理的拦截器：
    Authenticator mAuthenticator = new Authenticator() {
        @Override public Request authenticate(Route route, Response response)
                throws IOException {
            //Your.sToken = service.refreshToken();
            return response.request().newBuilder()
                    //.addHeader("Authorization", Your.sToken)
                    .build();
        }
    };

    private final Interceptor CACHE_CONTROL = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();
            if(false/*!NetUtils.hasNetwork(context)*/){
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
                //Logger.t(TAG).w("no network");
            }
            Response originalResponse = chain.proceed(request);
            if(true/*NetUtils.hasNetwork(context)*/){
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            }else{
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                        .removeHeader("Pragma")
                        .build();
            }
        }
    };


    //拦截器
    private final Interceptor LoggingInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.nanoTime();
            Log.d("cache",String.format("Sending request %s on %s%n%s", request.url(),  chain.connection(), request.headers()));
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();
            Log.d("cache",String.format("Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers()));
            return response;

        }
    };

    //访问时创建单例
    private static class SingletonHolder{
        private static final HttpService INSTANCE = new HttpService();
    }

    //获取单例
    public static HttpService getInstance(){
        return SingletonHolder.INSTANCE;
    }

    private static Observable.Transformer<?, ?> mTransformer = new Observable.Transformer<Object, Object>() {
        @Override
        public Observable<Object> call(Observable<Object> objectObservable) {
            return objectObservable.
                    subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    };

    @SuppressWarnings("unchecked")
    public static <T>Observable.Transformer<T, T> io_main(){
        return (Observable.Transformer<T, T>) mTransformer;
    }

    public static String getPackageName(){
        return HttpService.class.getPackage().getName();
    }
}
