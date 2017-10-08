package com.maijiaxiu.yjm.response;

import com.google.gson.annotations.SerializedName;
import com.maijiaxiu.yjm.entity.Category;
import com.maijiaxiu.yjm.entity.Pages;

import java.util.List;

/**
 * Created by yongjiaming on 17/9/19.
 */

public class QueryCategoryResponse {

    @SerializedName("data")
    public List<Category> data;
    @SerializedName("count")
    public int count;
    @SerializedName("pages")
    public Pages pages;

}
