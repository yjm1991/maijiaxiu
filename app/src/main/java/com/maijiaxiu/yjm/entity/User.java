package com.maijiaxiu.yjm.entity;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by yongjiaming on 17/10/25.
 */

public class User extends BmobObject implements Serializable {

    public String name;
    public String password;
    public String wechatOpenId;
    //已完成订单数
    public int complementOrder;
    //过期时间
    public long expireTimeStamp;
    //邀请人数
    public int inviteFriendsCount;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
