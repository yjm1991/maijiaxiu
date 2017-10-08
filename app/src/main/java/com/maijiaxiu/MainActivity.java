package com.maijiaxiu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tencent.smtt.sdk.WebView;

import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;


public class MainActivity extends AppCompatActivity {

    private WebView webview;

    //TODO 检测到有多个item则同时请求
    //FIXME 成功页面之后无法自动刷新

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) findViewById(R.id.webview);
        //登陆
        webview.loadUrl("http://www.maijiaxiuwang.com/common/buyer/login");

        MoneyWebviewPool.getInstance().init(this);

        //后台生成表
        //BmobUpdateAgent.initAppVersion();
        //检查更新
        BmobUpdateAgent.update(this);
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                // TODO Auto-generated method stub
                //根据updateStatus来判断更新是否成功
                Log.d("yjm", "updateStatus: " + updateStatus + "-----" + updateInfo.toString());
            }
        });
    }


    public void setUserAgent(String userAgent) {
        webview.getSettings().setUserAgentString(userAgent);
    }


}
