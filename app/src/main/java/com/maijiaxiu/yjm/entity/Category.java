package com.maijiaxiu.yjm.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yongjiaming on 17/9/19.
 * 商品信息
 */

public class Category {
    @SerializedName("id")
    public int id;
    @SerializedName("type")
//    {{#eq type 'A'}}
//    线下退货
//    {{/eq}}
//    {{#eq type 'D'}}
//    无需退货
//    {{/eq}}
//    {{#eq type 'F'}}
//    开始体验
//    {{/eq}}
    public String type;
    @SerializedName("cover")
    public String cover;
    @SerializedName("entry_type")
    public String entryType;
    @SerializedName("platform_id")
    public String platformId;
    @SerializedName("total")
    public int total;
    @SerializedName("doing")
    public int doing;
    @SerializedName("done")
    public int done;
    @SerializedName("released")
    public int released;
    @SerializedName("price")
    public String price;
    @SerializedName("ids")
    public String ids;
    @SerializedName("short_name")
    public String shortName;
}
