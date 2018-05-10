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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Cookie;

public class MaijiaxiuService extends Service {

    private String keyWords;
    private List<User> accountMap = new CopyOnWriteArrayList<>();
    private int index;
    private int delayMilliSecond = 300;

    private Handler handler = new Handler();

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
        delayMilliSecond = MyApplication.mConfiguration.delayRateInMills;
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
        //FIXME acco
        int temp = index % accountMap.size();
        final User user = accountMap.get(temp);
        RetrofitService.getInstance().setCurrentUser(user);
        Log.d("yjm", "query username=" + RetrofitService.getInstance().getCurrentUser().name);
        List<Cookie> cookieList = RetrofitService.getInstance().getUserCookie();
        index++;
        if (index > accountMap.size()) {
            index = 0;
        }

//        if(user.completOrder >= 2){
//            accountMap.remove(temp);
//        }
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
                        if (/*category.type.equals("A") ||*/ isTargetGoods(category.shortName)) {
                            hasAuction = true;
                            getToken(user, category);
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
                if (errorMsg.equals("code:429")) {
                    delayMilliSecond += 100;
                    handler.removeCallbacksAndMessages(null);
                }
                delayQuery();
            }
        });
    }

    private void delayQuery() {

        if (delayMilliSecond == -1) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        if (hours >= 0 && hours < 8) {
            delayMilliSecond = 60 * 60 * 1000;
        }


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                query();
            }
        }, delayMilliSecond);

    }


    private void sendNotification(User user, Category category) {
        //获取NotificationManager实例
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置通知标题
                .setContentTitle("刷单成功")
                //设置通知内容
                .setContentText(user.name + "------" + category.shortName)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        notifyManager.notify(category.id, builder.build());
    }

    private void fireAnOrder(final User user, final Category category, String token) {
        RetrofitService.getInstance().fireAnOrder(String.valueOf(category.id), category.ids.split(","), token, new INetWorkCallback<BaseResponse>() {
            @Override
            public void onResponse(BaseResponse baseResponse) {
                if (baseResponse.code >= 200 && baseResponse.code < 300) {

                }
                sendNotification(user, category);
                RetrofitService.getInstance().getCurrentUser().complementOrder++;
                delayQuery();
            }

            @Override
            public void onFailure(String errorMsg) {
                //出錯
                if (errorMsg.equals("code:429")) {
                    delayMilliSecond += 100;
                    if(delayMilliSecond >= 2000){
                        delayMilliSecond = MyApplication.mConfiguration.delayRateInMills;
                    }
                    handler.removeCallbacksAndMessages(null);
                }
                delayQuery();
            }
        });
    }

    private void getToken(final User user, final Category category) {
        RetrofitService.getToken(String.valueOf(category.id), category.ids.split(","), new INetWorkCallback<String>() {
            @Override
            public void onResponse(String html) {
                Document document = Jsoup.parse(html);
                String dataToken = document.getElementById("app").attr("data-token");
                Log.d("yjm", "dataToken=" + dataToken);
                fireAnOrder(user, category, dataToken);
            }

            @Override
            public void onFailure(String errorMsg) {
                delayQuery();
            }
        });
    }

    private boolean isTargetGoods(String shortName) {
        if (TextUtils.isEmpty(keyWords)) {
            return false;
        }
        String[] keyArray = keyWords.split(" ");
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
