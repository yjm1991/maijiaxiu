package com.maijiaxiu.yjm;

/**
 * Created by yongjiaming on 17/9/19.
 */

public interface INetWorkCallback<T> {

    void onResponse(T t);
    void onFailure(String errorMsg);
}
