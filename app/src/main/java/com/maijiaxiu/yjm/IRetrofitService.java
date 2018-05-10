package com.maijiaxiu.yjm;

import com.maijiaxiu.yjm.response.BaseResponse;
import com.maijiaxiu.yjm.response.QueryCategoryResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by yongjiaming on 17/9/18.
 */

public interface IRetrofitService {

    @FormUrlEncoded
    @PUT("/common/buyer/login")
    Call<BaseResponse> login(@FieldMap Map<String, String> loginRequest);

    //获取商品列表  试客专区
    @GET("/buyer/plan/type/A/category/T/stat/running")
    Call<QueryCategoryResponse> queryFreeGoods();

    //获取商品列表  图文评测
    @GET("/buyer/plan/type/A/category/A/stat/running")
    Call<QueryCategoryResponse> queryMoneyGoods();

    @FormUrlEncoded
    @PUT("/buyer/task")
    Call<BaseResponse> order(@Header("Referer") String referer, @Field("plan_id") String planId, @Field("_token") String token, @Field("plan_ids[]") String[] planIds);

    @GET("/buyer/plan/{id}?show_type=now")
    Call<String> getHtml(@Path("id") String id, @Query("ids") String ids);


}
