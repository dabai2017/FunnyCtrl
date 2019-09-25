package com.dabai.funny.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dabai.funny.R;
import com.dabai.funny.ctrls.Config;
import com.dabai.funny.utils.WifiAdmin;

import java.io.IOException;

public class ClientToolActivity extends AppCompatActivity {

    private String password, netWorkType, netWorkName;
    private WifiAdmin wifiAdmin;
    private ProgressDialog wifipd;
    private WifiManager mWifiManager;
    WifiInfo mWifiInfo;
String TAG = "dabai";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_tool);

        init();

        wifiAdmin = new WifiAdmin(getApplicationContext());

        apinit();
    }

    //初始化 wifi连接
    private void apinit() {

        /**
         * 初始化 wifi的连接
         * 然后 连接之后  把主机 和 自己的信息  写上去
         * 之后就是  测试  各种连接主机的方式  都有没有问题（浏览器打开链接添砖app ， 扫码 ， 地址连接）
         * 待测试结束   开始 写socket测试
         */


        password = Config.pwd;
        netWorkType = "WPA";
        netWorkName = Config.ssid;


        new Thread(new Runnable() {
            @Override
            public void run() {

                if (!wifiAdmin.mWifiManager.isWifiEnabled()) {
                    wifiAdmin.openWifi();
                }

                int net_type = 0x13;
                if (netWorkType
                        .compareToIgnoreCase("wpa") == 0) {
                    net_type = WifiAdmin.TYPE_WPA;// wpa
                } else if (netWorkType
                        .compareToIgnoreCase("wep") == 0) {
                    net_type = WifiAdmin.TYPE_WEP;// wep
                } else {
                    net_type = WifiAdmin.TYPE_NO_PASSWD;// 无加密
                }

                wifiAdmin.addNetwork(netWorkName, password, net_type);


                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (isNetworkOnline()) {

                            new AlertDialog.Builder(ClientToolActivity.this)
                                    .setMessage("连接成功了呢O(∩_∩)O").setTitle("提示")
                                    .setNeutralButton("亲自看一看", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {

                            new AlertDialog.Builder(ClientToolActivity.this)
                                    .setMessage("现在好像不能上网呦(T_T)").setTitle("提示")
                                    .setNeutralButton("亲自看一看", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setPositiveButton("OK", null).show();
                        }
                    }
                });

            }
        }).start();



    }


    public boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 www.baidu.com");
            int exitValue = ipProcess.waitFor();
            Log.i("Avalible", "Process:" + exitValue);
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }



    private void init() {
        getSupportActionBar().setElevation(0);
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
