package com.maijiaxiu.yjm;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by yongjiaming on 17/9/20.
 */

public class SaveCookiesInterceptor implements Interceptor {

    private Context context;

    public SaveCookiesInterceptor(Context context) {
        super();
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            Set<String> cookieSet = new HashSet<>();
            for (String header : originalResponse.headers("Set-Cookie")) {
                cookieSet.add(header.split(";")[0]);
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
            sharedPreferences.edit()
                    .putStringSet("cookie", cookieSet)
                    .apply();
        }

        return originalResponse;
    }
}
