package com.maijiaxiu.yjm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.maijiaxiu.yjm.entity.Configuration;
import com.maijiaxiu.yjm.entity.User;
import com.maijiaxiu.yjm.request.LoginRequest;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.key_word_et)
    EditText editText;
    @BindView(R.id.invite_code_et)
    EditText inviteCodeEt;
    @BindView(R.id.tips_tv)
    TextView tipTv;
    @BindView(R.id.qr_code_iv)
    ImageView qrCodeIv;
    MaijiaxiuService maijiaxiuService;
    ServiceConnection serviceConnection;
    private List<User> accountList = new ArrayList<>();

    private User currentUser;
    private static final long DAY = 24 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        checkUpdate();
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser != null) {
            accountList.add(currentUser);
        }
    }

    @OnClick(R.id.sure_btn)
    public void setKeyWord() {
        String keyWord = editText.getText().toString();
        if (maijiaxiuService != null && !TextUtils.isEmpty(keyWord)) {
            maijiaxiuService.setKeyWord(keyWord);
        }
        Toast.makeText(MainActivity.this, "关键字设置成功", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.stop_btn)
    public void stopMaijiaxiuService() {
        try{
            stopService(new Intent(MainActivity.this, MaijiaxiuService.class));
            if (serviceConnection != null) {
                unbindService(serviceConnection);
            }
            System.exit(0);
        } catch (Exception ignore){

        }
    }

    @OnClick(R.id.invite_sure_btn)
    public void enterInviteCode() {
        final String invitePhone = inviteCodeEt.getText().toString();
        if (TextUtils.isEmpty(invitePhone)) {
            Toast.makeText(MainActivity.this, "邀请人手机号为空", Toast.LENGTH_LONG).show();
            return;
        }
        if(invitePhone.equals(currentUser.name)){
            Toast.makeText(MainActivity.this, "输入自己的手机号无效", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(currentUser.invitePhone)) {
            BmobQuery<User> query = new BmobQuery<>();
            query.addWhereEqualTo("name", invitePhone);
            query.setLimit(1);
            query.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    if (list == null || list.size() == 0) {
                        Toast.makeText(MainActivity.this, "找不到邀请人的手机号", Toast.LENGTH_LONG).show();
                        return;
                    }
                    User inviter = list.get(0);
                    inviter.increment("inviteFriendsCount");
                    inviter.setValue("expireDate", new BmobDate(new Date(BmobDate.getTimeStamp(inviter.expireDate.getDate()) + 3 * DAY)));
                    inviter.update(inviter.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e != null){
                                Log.e("yjm", e.getMessage());
                            }
                            Toast.makeText(MainActivity.this, "恭喜邀请人和被邀请人分别增加三天试用期", Toast.LENGTH_LONG).show();
                        }
                    });
                    currentUser.setValue("invitePhone", invitePhone);
                    currentUser.setValue("expireDate", new BmobDate(new Date(BmobDate.getTimeStamp(currentUser.expireDate.getDate()) + 3 * DAY)));
                    currentUser.update(currentUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e != null){
                                Log.e("yjm", e.getMessage());
                            }
                        }
                    });
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "已输入过邀请人手机号，不能重复获取奖励", Toast.LENGTH_LONG).show();
        }
    }

    private void bindService() {
        if (BmobDate.getTimeStamp(currentUser.getUpdatedAt()) > BmobDate.getTimeStamp(currentUser.expireDate.getDate())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "已无剩余体验时长，请联系微信续费", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        final Intent intent = new Intent(this, MaijiaxiuService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d("yjm", "onServiceConnected invoke");

                maijiaxiuService = ((MaijiaxiuService.MyBuild) iBinder).getMyService();
                maijiaxiuService.setAccountMap(accountList);
                startService(intent);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("yjm", "onServiceDisconnected invoke");
            }
        };
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        stopService(new Intent(this, MaijiaxiuService.class));
    }

    private void initAccount() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), "买家秀.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.startsWith("#")) {
                            continue;
                        }
                        String[] account = line.split(" ");
                        accountList.add(new User(account[0], account[1]));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                if (accountList.size() == 0) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this, "账号和密码未填写或编辑错误", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    return;
//                }
                for (User user : accountList) {
                    login(user);
                }
                bindService();
            }
        }).start();
    }

    private void login(final User user) {
        RetrofitService.getInstance().setCurrentUser(user);
        RetrofitService.getInstance().login(new LoginRequest(user.name, "", user.password), new INetWorkCallback() {
            @Override
            public void onResponse(Object o) {

            }

            @Override
            public void onFailure(final String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void checkUpdate() {
        BmobUpdateAgent.setUpdateOnlyWifi(false);
        BmobUpdateAgent.update(this);
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                //根据updateStatus来判断更新是否成功
                Log.d("yjm", "onUpdateReturned updateStatus:" + updateStatus);
                //当前是最新版本才开始抢单
                if (updateStatus == 1) {
                    getConfiguration();
                }
            }
        });
    }


    private void getConfiguration() {
        BmobQuery<Configuration> query = new BmobQuery<>();
        query.getObject("3cc052935e", new QueryListener<Configuration>() {
            @Override
            public void done(Configuration configuration, BmobException e) {
                MyApplication.mConfiguration = configuration;
                tipTv.setText(configuration.tips);
                Glide.with(MainActivity.this)
                        .load(configuration.qrCode)
                        .into(qrCodeIv);
                initAccount();
            }
        });
    }
}
