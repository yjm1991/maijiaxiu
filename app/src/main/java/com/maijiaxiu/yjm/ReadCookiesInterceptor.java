package com.maijiaxiu.yjm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yongjiaming on 17/9/20.
 */

public class ReadCookiesInterceptor implements Interceptor {


    private Context context;

    public ReadCookiesInterceptor(Context context) {
        super();
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        SharedPreferences sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        //Set<String> cookie = sharedPreferences.getStringSet("cookie", new HashSet<String>());
//        builder.addHeader("Cookie", cookie);
        return chain.proceed(builder.build());
    }
}
