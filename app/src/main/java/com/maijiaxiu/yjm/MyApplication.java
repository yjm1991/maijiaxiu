package com.maijiaxiu.yjm;

import android.app.Application;

import cn.bmob.v3.Bmob;

/**
 * Created by yongjiaming on 17/9/15.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "d60cef766d84b232b22daa62bb3c9080", "bomb");

    }
}
