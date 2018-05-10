package com.maijiaxiu.yjm.entity;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;

/**
 * Created by yongjiaming on 17/10/25.
 */

public class User extends BmobObject implements Serializable {

    public String name;
    public String password;
    public String wechatOpenId = "";
    //已完成订单数
    public Integer complementOrder = 0;
    //过期时间
    public BmobDate expireDate;
    //邀请人数
    public Integer inviteFriendsCount = 0;
    //邀请人
    public String invitePhone = "";

    public User(){

    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @Override
    public void setCreatedAt(String createdAt) {
        super.setCreatedAt(createdAt);
    }
}
