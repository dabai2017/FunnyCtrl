package com.dabai.funny.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dabai.funny.R;
import com.dabai.funny.ctrls.Config;
import com.dabai.funny.service.ApServer;
import com.dabai.funny.utils.ApManager;
import com.dabai.funny.utils.DabaiUtils;
import com.dabai.funny.utils.QRutil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class ServiceActivity extends AppCompatActivity {
    private String TAG = "dabai";
    private Context context;
    private Intent scintent;

    TextView te1, te2, te3, te4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        context = getApplicationContext();

        scintent = new Intent(this, ApServer.class);
        checkAp();

        try {
            init();
            init_val();
        } catch (Exception e) {
            Toast.makeText(context, "出了一些小问题~\n" + e, Toast.LENGTH_SHORT).show();
        }

        handler.post(task);

        Config.ssid = "正在启动服务";
        Config.pwd="null";
        Config.IP="null";
        Config.IPsum = "0";

    }

    private void init_val() {
        //控件实例化

        te1 = findViewById(R.id.textView);
        te2 = findViewById(R.id.textView2);
        te3 = findViewById(R.id.textView3);
        te4 = findViewById(R.id.textView4);
    }

    private void init() {
        getSupportActionBar().setElevation(0);
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void checkAp() {
        if (ApManager.isApOn(context)) {

            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("请先关闭WiFi热点")
                    .positiveText("关闭")
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            closeAps();

                        }
                    })
                    .show();

        } else {

            try {
                // Android 8.0使用startForegroundService在前台启动新服务
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(scintent);
                } else {
                    context.startService(scintent);
                }
            } catch (Exception e) {
                context.startService(scintent);
            }
        }
    }

    private void closeAps() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method method = wifiManager.getClass().getMethod("cancelLocalOnlyHotspotRequest");
            method.setAccessible(true);
            method.invoke(wifiManager);
            stopService(scintent);
            finish();

        } catch (Exception e) {
            Toast.makeText(context, "异常:" + e, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            stopService(scintent);
            finish();
            closeAps();


            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {
            handler.postDelayed(this, 1 * 1000);//设置循环时间，此处是5秒


            //刷新 ap配置信息

            te1.setText("热点名称 : " + Config.ssid);
            te2.setText("密码 : " + Config.pwd);
            te3.setText("连接数 : " + Config.IPsum);
            te4.setText("IP 地址 : " + Config.IP);

        }
    };


    String fun;
    public void share(View view) {
//分享链接 服务器  借鉴qq面对面

        View v = getLayoutInflater().inflate(R.layout.dialog_qrshare,null);


        new MaterialDialog.Builder(this)
                .title("分享服务器")
                .customView(v, true)
                .positiveText("关闭")
                .show();

        fun = ("https://dabai2017.github.io/?tip="+Build.MODEL+"&ssid="+Config.ssid+"&pass="+Config.pwd+
        "&ip="+Config.IP).replace(" ","");

        Log.d(TAG, fun.split("/")[3]);




        Bitmap bit = QRutil.createQRCodeBitmap(fun, 700, 700, "UTF-8", "H", "1", QRutil.QRColor, QRutil.QRBackColor);
        ImageView img = v.findViewById(R.id.imageView);
        img.setImageBitmap(bit);

    }

    public void share_text(View view) {

        new DabaiUtils().sendText(context,fun);

    }





}
