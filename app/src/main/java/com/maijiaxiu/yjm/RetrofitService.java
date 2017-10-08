package com.maijiaxiu.yjm;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maijiaxiu.yjm.request.LoginRequest;
import com.maijiaxiu.yjm.response.BaseResponse;
import com.maijiaxiu.yjm.response.QueryCategoryResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yongjiaming on 17/9/19.
 */

public class RetrofitService {

    private IRetrofitService retrofitService;
    private static final String BASE_URL = "http://www.maijiaxiuwang.com";
    private final static int READ_TIMEOUT = 30;
    private final static int CONNECT_TIMEOUT = 10;

    private static RetrofitService mInstance;


    private RetrofitService() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .addInterceptor(new SaveCookiesInterceptor(applicationCtx))
//                .addInterceptor(new ReadCookiesInterceptor(applicationCtx))
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        Log.d("yjm2", "saveFromResponse: " + url.toString());
                        if(!cookieStore.containsKey(url.host())){
//                            List<Cookie> oldCookie = cookieStore.get(url.host());
//                            cookieStore.put(url.host(), oldCookie);
                            cookieStore.put(url.host(), cookies);
                        } else{

                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        Log.d("yjm2", "loadForRequest: " + url.toString());
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                ;

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(httpLoggingInterceptor);

        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder.build())
                .build();

        retrofitService = mRetrofit.create(IRetrofitService.class);

        mInstance = this;
    }


    public static RetrofitService getInstance(){
        if(mInstance == null){
            mInstance = new RetrofitService();
        }
        return mInstance;
    }


    public void login(LoginRequest loginRequest, final INetWorkCallback callback) {
        Map<String, String> loginMap = new LinkedHashMap<>();
        loginMap.put("name", loginRequest.name);
        loginMap.put("wechat_openID", loginRequest.wechatOpenID);
        loginMap.put("password", loginRequest.password);
        Call<BaseResponse> call = retrofitService.login(loginMap);
        enqueue(call, callback);
    }

    //查询抢购商品列表
    public void queryAuctionCategory(INetWorkCallback<QueryCategoryResponse> callback){
        Call<QueryCategoryResponse> categoryResponseCall = retrofitService.queryAuctionCategory(1, 50);
        enqueue(categoryResponseCall, callback);
    }


    //抢购
    public void fireAnOrder(String planId, String[] ids, INetWorkCallback<BaseResponse> callback){
        if(ids == null || ids.length == 0){
            ids = new String[]{planId};
        }
        Call<BaseResponse> call = retrofitService.order(planId, ids);
        enqueue(call, callback);
    }

    private void enqueue(Call call, final INetWorkCallback callback) {
        if (call == null) {
            return;
        }
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d("yjm", "code:" + response.code());
                //保存header信息
                if (response.code() >= 200 && response.code() < 300) {
                    if(null != response.body()){
                        callback.onResponse(response.body());
                    } else{
                        callback.onResponse(response);
                    }
                } else {
                    callback.onFailure("code:" + response.code());
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("yjm", t.getMessage());
                callback.onFailure(t.getMessage());
            }
        });
    }

}
