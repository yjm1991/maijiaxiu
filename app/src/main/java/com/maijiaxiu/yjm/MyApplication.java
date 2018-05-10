package com.maijiaxiu.yjm;

import android.app.Application;

import com.maijiaxiu.yjm.entity.Configuration;
import com.tencent.bugly.crashreport.CrashReport;

import cn.bmob.v3.Bmob;

/**
 * Created by yongjiaming on 17/9/15.
 */

public class MyApplication extends Application {


    public static Configuration mConfiguration;

    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "d60cef766d84b232b22daa62bb3c9080", "bomb");
        CrashReport.initCrashReport(getApplicationContext(), "fdcdbc25a7", true);
    }
}
