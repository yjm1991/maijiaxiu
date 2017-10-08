package com.maijiaxiu;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

/**
 * Created by yongjiaming on 17/9/14.
 */

public class MoneyWebview extends com.tencent.smtt.sdk.WebView {

    private Context context;

    private static final String DOMAIN = "http://www.maijiaxiuwang.com";
    private static final String LOGIN_URL = DOMAIN + "/common/buyer/login";
    //无需退货
//    private static final String GIFT_URL = "http://www.maijiaxiuwang.com/buyer/plans?type=gift&category=photo";
    //线下退货
    private static final String MONEY_URL = "http://www.maijiaxiuwang.com/buyer/plans?type=now&category=photo";
    private static final String MONEY_URL2 = "http://www.maijiaxiuwang.com/buyer/plans?category=photo&type=now";
    private static final String PREVIEW_URL = "http://www.maijiaxiuwang.com/buyer/plans?type=preview&category=photo";
    private static final String GIFT_URL = MONEY_URL;

    private static final String SCRIPT_GET_GIFT_URL = "(function(){var gift = document.getElementsByClassName('item gift'); if(gift.length==0){return 0} else{ return document.getElementsByClassName('item gift')[0].getAttribute('data-url')}})();";
    //private static final String MAKE_MONEY_SCRIPT = "(function(){var items = document.getElementsByClassName('content');if(items.length==1){return 0} else{ var  result=''; for(var i=1;i<items.length;i++){result+=items[i].parentNode.getAttribute('data-url');if(i!=items.length-1){result+=','};}return result}})();";
    private static final String MAKE_MONEY_SCRIPT = "(function(){var items = document.getElementsByClassName('u-good-item');if(items.length==0){return 0} else{ var  result=''; for(var i=0;i<items.length;i++){if(items[i].className=='u-good-item'&&items[i].innerText.indexOf('线下退货')!=-1){items[i].click();return 'fire';}}}  return result;})();";
    private static final String CLICK_BUY_BTN_SCRIPT = "(function(){var buyBtn = document.getElementsByClassName('ui button'); if(buyBtn == null || buyBtn[0] == null){return 0} else{buyBtn[0].click(); return 1;}})();";
    private static final String giftDetailUrl = "http://www.maijiaxiuwang.com/buyer/plan";
    //详情页的标志
    private static final String giftDetailFlag = "show_type=now&ids";


    //是否可见状态，不可见下的只抢单,不循环
    private boolean isVisible = true;

    public MoneyWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MoneyWebview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public MoneyWebview(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MoneyWebview(Context context, boolean isVisible) {
        super(context);
        this.context = context;
        this.isVisible = isVisible;
        init();
    }

    private void init() {
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);

                Log.d("yjm2", "onPageFinished url: " + url);
                if (isVisible) {
                    if (url.equals(GIFT_URL) || url.equals(MONEY_URL2)) {
                        clickGiftItem();
                        return;
                    }

                    if (url.startsWith(giftDetailUrl) && url.contains(giftDetailFlag)) {
                        Log.d("yjm2", "clickBuyBtn invoke");
                        clickBuyBtn();
                        //抢购失败重新刷新
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (url.contains(giftDetailUrl)) {
                                    refreshGiftPage();
                                }
                            }
                        }, 1000);
                        return;
                    }

                    //抢购成功立马刷新
                    if (url.contains("buyer/task")) {
                        refreshGiftPage();
                    }
                } else {
                    Log.d("yjm2", "并发进入抢购详情页: " + url);
                    clickBuyBtn();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                return super.shouldOverrideUrlLoading(webView, s);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
                Log.d("shouldInterceptRequest", "s: " + s);
                return super.shouldInterceptRequest(webView, s);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
                Log.d("shouldInterceptRequest", "s: " + webResourceRequest.getUrl());
                if(!interceptRequest(webResourceRequest.getUrl().toString())){
                    return super.shouldInterceptRequest(webView, webResourceRequest);
                }
                return null;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
                return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
            }
        });

        setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Log.d("progress", "preogress: " + newProgress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d("yjm2", "url: " + url + ", message: " + message);
                return super.onJsAlert(view, url, message, result);
            }
        });
    }


    private void clickGiftItem() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                evaluateJavascript(MAKE_MONEY_SCRIPT, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.d("yjm2", "onReceiveValue: " + value);
                        if (!"fire".equals(value)) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loadUrl(GIFT_URL);
                                }
                            }, 1000);
                        }/* else {
                            value = value.substring(1, value.length() - 1);
                            Log.d("yjm2", "去掉引号后:" + value);
                            String[] paramArray = value.split(",");
                            //多个列表则多个webview去抢
                            for (int i = paramArray.length - 1; i > 0; i--) {
                                MoneyWebviewPool.getInstance().getWebView().loadUrl(DOMAIN + paramArray[i]);
                            }
                            goToGiftDetailPage(paramArray[0]);
                        }*/
                    }
                });
            }
        }, 500);
    }

//    private void goToGiftDetailPage(String param) {
//        loadUrl(DOMAIN + param);
//        giftDetailUrl = DOMAIN + param;
//        Log.d("yjm2", "goToGiftDetailPage url: " + DOMAIN + param);
//    }


    private int clickButBtnRetryTime = 0;
    private void clickBuyBtn() {
        clickButBtnRetryTime++;
        evaluateJavascript(CLICK_BUY_BTN_SCRIPT, new ValueCallback<String>(){
            @Override
            public void onReceiveValue(String s) {
                Log.d("onReceiveValue", s + "clickButBtnRetryTime: " + clickButBtnRetryTime);
                if("0".equals(s) && clickButBtnRetryTime < 4){
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clickBuyBtn();
                        }
                    }, 100);
                } else{
                    clickButBtnRetryTime = 0;
                }
            }
        });

    }

    private void refreshGiftPage() {
        loadUrl(GIFT_URL);
    }

    private boolean interceptRequest(String url){
        if(url.endsWith("png") || url.endsWith("jpg") || url.endsWith("jpeg") || url.endsWith("gif")){
            return true;
        }
        return false;
    }
}
