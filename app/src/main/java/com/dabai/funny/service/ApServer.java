package com.dabai.funny.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.dabai.funny.R;
import com.dabai.funny.ctrls.Config;
import com.dabai.funny.utils.ApManager;

public class ApServer extends Service {

    private String TAG = "dabai";
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                        super.onStarted(reservation);
                        String ssid = reservation.getWifiConfiguration().SSID;
                        String pwd = reservation.getWifiConfiguration().preSharedKey;

                        Config.ssid = ssid;
                        Config.pwd = pwd;
                        Config.IP = ApManager.getHotspotLocalIpAddress(getApplicationContext());


                        try {
                            //如果API在26以上即版本为O则调用startForefround()方法启动服务
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                setForegroundService();
                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onStopped() {
                        super.onStopped();

                        Toast.makeText(getApplicationContext(), "服务端被迫停止", Toast.LENGTH_SHORT).show();
                        notificationManager.cancel(9);
                    }

                    @Override
                    public void onFailed(int reason) {
                        super.onFailed(reason);
                        Toast.makeText(getApplicationContext(), "服务启动失败", Toast.LENGTH_SHORT).show();
                        notificationManager.cancel(9);
                    }
                }, null);
            } catch (Exception e) {
                Toast.makeText(this, "程序异常了哦", Toast.LENGTH_SHORT).show();
                try {
                    notificationManager.cancel(9);
                } catch (Exception ex) {
                    Toast.makeText(this, "程序异常了哦"+ex, Toast.LENGTH_SHORT).show();

                }
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 通过通知启动服务
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void setForegroundService() {
        //设定的通知渠道名称
        String channelName = "滑稽服务端^前台服务";
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        String CHANNEL_ID = "8";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);

        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stat_name) //设置通知图标
                .setColor(Color.parseColor("#69F0AE"))
                .setContentText("滑稽服务正在运行")//设置通知内容
                .setOngoing(true);//设置处于运行状态
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(9, builder.build());

    }


}
