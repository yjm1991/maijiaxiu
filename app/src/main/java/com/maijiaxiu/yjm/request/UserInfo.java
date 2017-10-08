package com.maijiaxiu.yjm.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yongjiaming on 17/9/19.
 */

public class UserInfo {
    @SerializedName("name")
    public String name;
    @SerializedName("wechat_openID")
    public String wechatOpenID;
    @SerializedName("password")
    public String password;
}
