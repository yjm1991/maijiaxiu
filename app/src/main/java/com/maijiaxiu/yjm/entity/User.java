package com.maijiaxiu.yjm.entity;

/**
 * Created by yongjiaming on 17/10/25.
 */

public class User {

    public String name;
    public String password;
    public String wechatOpenId;
    //已完成订单数
    public int completOrder;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
