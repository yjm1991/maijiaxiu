package com.maijiaxiu.yjm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.maijiaxiu.yjm.entity.Category;
import com.maijiaxiu.yjm.response.BaseResponse;
import com.maijiaxiu.yjm.response.QueryCategoryResponse;

public class MaijiaxiuService extends Service {

    //同时抢购的数量
    private int orderSameTimeCount = 2;

    public MaijiaxiuService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置通知标题
                .setContentTitle("买家秀")
                //设置通知内容
                .setContentText("刷单中")
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        query();
        return START_STICKY;
    }

    private void query() {
        RetrofitService.getInstance().queryAuctionCategory(new INetWorkCallback<QueryCategoryResponse>() {
            @Override
            public void onResponse(QueryCategoryResponse queryCategoryResponse) {
                boolean hasAuction = false;
                if (queryCategoryResponse.count == 0) {
                    Log.d("yjm", "沒用商品");
                } else {
                    int i = 0;
                    for (Category category : queryCategoryResponse.data) {
                        if (category.type.equals("A") && i < orderSameTimeCount) {
                            hasAuction = true;
                            i++;
                            fireAnOrder(category);
                        }
                    }
                }
                //没有线下退货则继续查询
                if (!hasAuction) {
                    delayQuery();
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.d("yjm", "query onFailure: " + errorMsg);
                delayQuery();
            }
        });
    }

    private void delayQuery() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                query();
            }
        }, 2000);
    }

    private void sendNotification(Category category) {
        //获取NotificationManager实例
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置通知标题
                .setContentTitle("刷单成功")
                //设置通知内容
                .setContentText(category.shortName);
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //.setWhen(System.currentTimeMillis());
        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        notifyManager.notify(1, builder.build());
    }

    private void fireAnOrder(final Category category) {
        RetrofitService.getInstance().fireAnOrder(String.valueOf(category.id), category.ids.split(","), new INetWorkCallback<BaseResponse>() {
            @Override
            public void onResponse(BaseResponse baseResponse) {
                delayQuery();
                sendNotification(category);
                if (baseResponse.code >= 200 && baseResponse.code < 300) {

                }
            }

            @Override
            public void onFailure(String errorMsg) {
                delayQuery();
            }
        });
    }
}
