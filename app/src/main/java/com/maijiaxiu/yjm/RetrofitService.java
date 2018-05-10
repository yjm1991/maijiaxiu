package com.maijiaxiu.yjm;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maijiaxiu.yjm.entity.User;
import com.maijiaxiu.yjm.request.LoginRequest;
import com.maijiaxiu.yjm.response.BaseResponse;
import com.maijiaxiu.yjm.response.QueryCategoryResponse;

import java.io.IOException;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by yongjiaming on 17/9/19.
 */

public class RetrofitService {

    private IRetrofitService retrofitService;
    private static final String BASE_URL = "http://www.maijiaxiuwang.com";
    private final static int READ_TIMEOUT = 30;
    private final static int CONNECT_TIMEOUT = 10;

    private static RetrofitService mInstance;
    private static CookieJar mCookieJar;
    private static HashMap<String, HashMap<String, List<Cookie>>> cookieHashMap = new HashMap<>();

    private User currentUser;

    private RetrofitService() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        mCookieJar = new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                Log.d("yjm2", "saveFromResponse currentUser: " + currentUser.name + "....." + url.toString());
                HashMap<String, List<Cookie>> currentUserCookie = cookieHashMap.get(currentUser.name);
                if (currentUserCookie == null) {
                    HashMap<String, List<Cookie>> hashMap = new HashMap<>();
                    hashMap.put(url.host(), cookies);
                    cookieHashMap.put(currentUser.name, hashMap);
                    return;
                }
                if (!currentUserCookie.containsKey(url.host())) {
                    currentUserCookie.put(url.host(), cookies);
                } else {

                }
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                 Log.d("yjm2", "loadForRequest currentUser:" + currentUser.name + "....." + url.toString());
                HashMap<String, List<Cookie>> currentUserCookie = cookieHashMap.get(currentUser.name);
                if (currentUserCookie == null) {
                    return new ArrayList<>();
                }
                List<Cookie> cookies = currentUserCookie.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .addInterceptor(new SaveCookiesInterceptor(applicationCtx))
//                .addInterceptor(new ReadCookiesInterceptor(applicationCtx))
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new HeadersInterceptor())
                .cookieJar(mCookieJar);

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


    public static RetrofitService getInstance() {
        if (mInstance == null) {
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
//        try {
//            call.execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private volatile int fireCount = 1;

    //查询抢购商品列表
    public void queryAuctionCategory(INetWorkCallback<QueryCategoryResponse> callback) {
        if(fireCount % 2 == 0){
            fireCount = 1;
            Call<QueryCategoryResponse> categoryResponseCall = retrofitService.queryFreeGoods();
            enqueue(categoryResponseCall, callback);
        } else{
            Call<QueryCategoryResponse> categoryResponseCall = retrofitService.queryMoneyGoods();
            enqueue(categoryResponseCall, callback);
            fireCount++;
        }
    }

    //抢购
    public void fireAnOrder(String planId, String[] ids, String token, INetWorkCallback<BaseResponse> callback) {
        if (ids == null || ids.length == 0) {
            ids = new String[]{planId};
        }
        Call<BaseResponse> call = retrofitService.order(getProductDetailUrl(planId, ids), planId, token, ids);
        enqueue(call, callback);
    }

    public static void getToken(String planId, String[] ids, final INetWorkCallback<String> callback){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .addInterceptor(new SaveCookiesInterceptor(applicationCtx))
//                .addInterceptor(new ReadCookiesInterceptor(applicationCtx))
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new HeadersInterceptor())
                .cookieJar(mCookieJar);

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(builder.build())
                .build();
        IRetrofitService htmlService = retrofit.create(IRetrofitService.class);
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i< ids.length; i++){
            sb.append(ids[i]);
            if(i < ids.length - 1){
                sb.append(",");
            }
        }

        Call<String> call = htmlService.getHtml(planId, sb.toString());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    private static String getProductDetailUrl(String planId, String[] ids){
        //http://www.maijiaxiuwang.com/buyer/plan/394616?show_type=now&ids=394616,394617
        StringBuffer sb = new StringBuffer();
        sb.append("http://www.maijiaxiuwang.com/buyer/plan/")
                .append(planId)
                .append("?show_type=now&ids=");
        for(int i = 0; i< ids.length; i++){
            sb.append(ids[i]);
            if(i < ids.length - 1){
                sb.append(",");
            }
        }
        return sb.toString();
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
                    if (null != response.body()) {
                        callback.onResponse(response.body());
                    } else {
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


    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public List<Cookie> getUserCookie() {
        HashMap<String, List<Cookie>> cookie = cookieHashMap.get(currentUser.name);
        if (cookie == null) {
            return new ArrayList<>();
        }
        return cookieHashMap.get(currentUser.name).get("www.maijiaxiuwang.com");
    }


}
