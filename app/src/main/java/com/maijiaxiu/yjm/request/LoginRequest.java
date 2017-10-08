package com.maijiaxiu.yjm.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yongjiaming on 17/9/18.
 */

public class LoginRequest {

    @SerializedName("name")
    public String name;
    @SerializedName("wechat_openID")
    public String wechatOpenID;
    @SerializedName("password")
    public String password;

    public LoginRequest(String name, String wechatOpenID, String password) {
        this.name = name;
        this.wechatOpenID = wechatOpenID;
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "name='" + name + '\'' +
                ", wechatOpenID='" + wechatOpenID + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
