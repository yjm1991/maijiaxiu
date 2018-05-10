package com.maijiaxiu.yjm.entity;

import java.io.File;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by yongjiaming on 2018/3/9.
 */

public class Configuration extends BmobObject {
    //刷单间隔时长
    public int delayRateInMills = 300;
    //提示语
    public String tips = "hello world";
    //二维码
    public String qrCode = "https://sp0.baidu.com/5aU_bSa9KgQFm2e88IuM_a/micxp1.duapp.com/qr.php?value=yongjiaming";
    //抢单开关
    public boolean enable;
}
