package com.maijiaxiu.yjm;

import com.maijiaxiu.yjm.response.BaseResponse;
import com.maijiaxiu.yjm.response.QueryCategoryResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by yongjiaming on 17/9/18.
 */

public interface IRetrofitService {

    @FormUrlEncoded
    @PUT("/common/buyer/login")
    Call<BaseResponse> login(@FieldMap Map<String, String> loginRequest);

    //获取商品列表
    @GET("buyer/plan/type/now/category/photo")
    Call<QueryCategoryResponse> queryAuctionCategory(@Query("p") int p, @Query("pc") int pc);

    @FormUrlEncoded
    @PUT("/buyer/task")
    Call<BaseResponse> order(@Field("plan_id") String planId, @Field("plan_ids[]") String[] planIds);
}
