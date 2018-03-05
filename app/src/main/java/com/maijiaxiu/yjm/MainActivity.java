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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maijiaxiu.yjm.entity.User;
import com.maijiaxiu.yjm.request.LoginRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button confirmBtn;
    MaijiaxiuService maijiaxiuService;
    ServiceConnection serviceConnection;
    private List<User> accountList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editText =  findViewById(R.id.key_word_et);
        confirmBtn = findViewById(R.id.sure_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyWord = editText.getText().toString();
                if (maijiaxiuService != null && !TextUtils.isEmpty(keyWord)) {
                    maijiaxiuService.setKeyWord(keyWord);
                }
                Toast.makeText(MainActivity.this, "关键字设置成功", Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.stop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMaijiaxiuService();
            }
        });
        findViewById(R.id.add_contact_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        checkUpdate();
        User user = (User) getIntent().getSerializableExtra("user");
        if(user != null){
            accountList.add(user);
        }
    }

    private void bindService() {
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
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    private void stopMaijiaxiuService() {
        stopService(new Intent(MainActivity.this, MaijiaxiuService.class));
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
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
                        if(line.startsWith("#")){
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
                user.save();
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

    private void checkUpdate(){
        BmobUpdateAgent.setUpdateOnlyWifi(false);
        BmobUpdateAgent.update(this);
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                //根据updateStatus来判断更新是否成功
                Log.d("yjm", "onUpdateReturned updateStatus:" + updateStatus);
                //当前是最新版本才开始抢单
                if(updateStatus == 1){
                    initAccount();
                }
            }
        });
    }
}
