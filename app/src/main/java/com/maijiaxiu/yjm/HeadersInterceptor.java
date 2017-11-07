package com.maijiaxiu.yjm;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class HeadersInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(setCustomHeads(request));
    }

    private static Request setCustomHeads(Request request) {
        //String userAgent = System.getProperty("http.agent");
        String userAgent = "Mozilla/5.0 (Linux; U; Android 7.1.1; zh-CN; MZ-PRO 6 Build/MRA58K) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 MZBrowser/6.7.3 UWS/2.11.0.22 Mobile Safari/537.36";
        Request.Builder builder = request.newBuilder()
                .header("User-Agent", userAgent)
                .method(request.method(), request.body());
        Request newRequest = builder.build();
        return newRequest;
    }
}
