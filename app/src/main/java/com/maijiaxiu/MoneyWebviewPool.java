package com.maijiaxiu;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yongjiaming on 17/9/15.
 */

public class MoneyWebviewPool {

    private static MoneyWebviewPool instance;
    private static List<MoneyWebview> webviewList = new ArrayList();

    int index = 0;

    public static MoneyWebviewPool getInstance(){
        if(instance == null){
            instance = new MoneyWebviewPool();
        }
        return instance;
    }

    public void init(Context context){
        for(int i = 0; i < 3; i++){
            MoneyWebview webview = new MoneyWebview(context, false);
            webviewList.add(webview);
        }
    }


    public MoneyWebview getWebView(){
        index++;
        return webviewList.get(index % 3);
    }


}
