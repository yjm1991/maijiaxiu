package com.maijiaxiu.yjm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.maijiaxiu.yjm.entity.Category;
import com.maijiaxiu.yjm.entity.User;
import com.maijiaxiu.yjm.response.BaseResponse;
import com.maijiaxiu.yjm.response.QueryCategoryResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Cookie;

public class MaijiaxiuService extends Service {

    private String keyWords;
    private List<User> accountMap = new ArrayList<>();
    private int index;
    private int delayMilliSecond = 1000;

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
        return new MyBuild();
    }


    class MyBuild extends Binder {
        public MaijiaxiuService getMyService() {
            return MaijiaxiuService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        query();
        return START_STICKY;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        delayMilliSecond = -1;
    }

    private void query() {
        int temp = index % accountMap.size();
        User user = accountMap.get(temp);
        RetrofitService.getInstance().setCurrentUser(user);
        Log.d("yjm", "query username=" + RetrofitService.getInstance().getCurrentUser().name);
        List<Cookie> cookieList = RetrofitService.getInstance().getUserCookie();
        index++;
        if (index > accountMap.size()) {
            index = 0;
        }

        if(user.completOrder >= 2){
            accountMap.remove(temp);
        }
        if (cookieList == null || cookieList.size() == 0) {
            delayQuery();
            return;
        }
        RetrofitService.getInstance().queryAuctionCategory(new INetWorkCallback<QueryCategoryResponse>() {
            @Override
            public void onResponse(QueryCategoryResponse queryCategoryResponse) {
                boolean hasAuction = false;
                if (queryCategoryResponse.count == 0) {
                    Log.d("yjm", "无商品");
                } else {
                    for (Category category : queryCategoryResponse.data) {
                        if ((category.type.equals("A") || isTargetGoods(category.shortName))){
                            hasAuction = true;
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

        if(delayMilliSecond == -1){
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        if (minutes <= 10 || minutes >= 55) {
            delayMilliSecond = 0;
        } else {
            delayMilliSecond = 2000;
        }
        if (hours >= 0 && hours < 8) {
            delayMilliSecond = 60 * 60 * 1000;
        }



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                query();
            }
        }, delayMilliSecond);

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
                .setContentText(RetrofitService.getInstance().getCurrentUser().name + "------" + category.shortName)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        notifyManager.notify(category.id, builder.build());
    }

    private void fireAnOrder(final Category category) {
        RetrofitService.getInstance().fireAnOrder(String.valueOf(category.id), category.ids.split(","), new INetWorkCallback<BaseResponse>() {
            @Override
            public void onResponse(BaseResponse baseResponse) {
                if (baseResponse.code >= 200 && baseResponse.code < 300) {

                }
                sendNotification(category);
                RetrofitService.getInstance().getCurrentUser().completOrder++;
                delayQuery();
            }

            @Override
            public void onFailure(String errorMsg) {
                //出錯
                delayQuery();
            }
        });
    }

    private boolean isTargetGoods(String shortName) {
        if (TextUtils.isEmpty(keyWords)) {
            return false;
        }
        String[] keyArray = keyWords.split("#");
        for (String key : keyArray) {
            if (shortName.contains(key) || key.contains(shortName)) {
                return true;
            }
        }
        return false;
    }

    public void setKeyWord(String keyWord) {
        this.keyWords = keyWord;
    }

    public void setAccountMap(List<User> accountMap) {
        this.accountMap = accountMap;
    }
}
