package com.dabai.funny.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dabai.funny.R;
import com.dabai.funny.ctrls.Config;
import com.dabai.funny.utils.ApManager;
import com.dabai.funny.utils.DabaiUtils;
import com.dabai.funny.utils.QRutil;
import com.easysocket.EasySocket;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.IsReconnect;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServiceActivity extends AppCompatActivity {

    private static String TAG = "dabai";
    private Context context;


    TextView te1, te2, te3, te4;

    int mode = 0;
    private DatagramSocket ds;
    private StringBuffer sb;
    private Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        context = getApplicationContext();


        pro_check();


        try {
            init();
            init_val();
        } catch (Exception e) {
            Toast.makeText(context, "出了一些小问题~\n" + e, Toast.LENGTH_SHORT).show();
        }

        handler.post(task);

        Config.ssid = "未配置";
        Config.pwd = "null";
        Config.IP = "null";
        Config.IPsum = "0";

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == 200) {
            int checkResult16 = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            //if(!=允许),抛出异常
            if (checkResult16 == PackageManager.PERMISSION_GRANTED) {
                show_WIFIAP_dialog();
            } else {
                finish();
                Toast.makeText(context, "不给权限别用了", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void pro_check() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {//未开启定位权限
            Toast.makeText(this, "需要开启定位权限！", Toast.LENGTH_SHORT).show();
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {

            show_WIFIAP_dialog();

        }
    }


    private void show_WIFIAP_dialog() {

        if (isWiFiActive(context)) {
            //Log.d(TAG, "show_WIFIAP_dialog: WiFi连接");
            mode = 1;
            showWIFI_config();
        } else {
            //检测热点连接
            if (ApManager.isApOn(context)) {
                //热点连接
                //Log.d(TAG, "show_WIFIAP_dialog: 热点连接");
                mode = 2;
                showAP_config();
            } else {
                //无连接，退出
                new MaterialDialog.Builder(this)
                        .title("警告")
                        .content("没有任何连接，不能启动服务。\n所以，WiFi和开启热点必须选一个")
                        .positiveText("退出")
                        .cancelable(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                finish();
                            }
                        })
                        .show();
            }
        }

    }

    private void showWIFI_config() {
        final View v = getLayoutInflater().inflate(R.layout.dialog_wificonfig, null);
        final TextInputEditText ed1 = v.findViewById(R.id.ed1);
        final TextInputEditText ed2 = v.findViewById(R.id.ed2);

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        ed1.setText(wifiInfo.getSSID().replace("\"", ""));

        new MaterialDialog.Builder(this)
                .title("WiFi验证")
                .cancelable(false)
                .customView(v, true)
                .positiveText("确定")
                .neutralText("退出")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        String id = ed1.getText().toString();
                        String pass = ed2.getText().toString();

                        Config.ssid = id;
                        Config.pwd = pass;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                openLin();
                            }
                        }).start();

                    }
                }).onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                finish();
            }
        })
                .show();
    }

    private void showAP_config() {
        final View v = getLayoutInflater().inflate(R.layout.dialog_wificonfig, null);
        final TextInputEditText ed1 = v.findViewById(R.id.ed1);
        final TextInputEditText ed2 = v.findViewById(R.id.ed2);

        ed1.setText(getWifiApSSID(context));

        new MaterialDialog.Builder(this)
                .title("热点验证")
                .cancelable(false)
                .customView(v, true)
                .positiveText("确定")
                .neutralText("退出")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Config.IP = ApManager.getHotspotLocalIpAddress(getApplicationContext());

                        String id = ed1.getText().toString();
                        String pass = ed2.getText().toString();

                        Config.ssid = id;
                        Config.pwd = pass;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                openLin();
                            }
                        }).start();


                    }
                }).onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                finish();
            }
        }).show();
    }


    /**
     * 开启监听客户消息
     *
     * @return
     */

    public void openLin() {


        //开启socket监听,
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(7657);
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "7657端口被占用", Toast.LENGTH_SHORT).show();
                }
            });
        }

        while (true) {

            InputStream inputStream = null;
            try {
                socket = serverSocket.accept(); //开始监听5040端口
                inputStream = socket.getInputStream();
                int lenght = 0;
                byte[] buff = new byte[1024];
                sb = new StringBuffer();
                while ((lenght = inputStream.read(buff)) != -1) {
                    sb.append(new String(buff, 0, lenght, "UTF-8"));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    //检查来自7657的消息
                        checkMsg(sb.toString());
                    }
                });
                socket.shutdownInput();
                OutputStream os = socket.getOutputStream();
                String string = new String(("欢迎连接" + Build.MODEL + "的服务器~").getBytes(), "UTF-8");
                os.write(string.getBytes());
                os.flush();
                // 关闭输出流
                socket.shutdownOutput();
                os.close();
                // 关闭IO资源
                inputStream.close();
            } catch (IOException e) {
                Log.d(TAG, "openLin: " + e);
            }

        }
    }


    Map people = new HashMap();


    public void f5(){
        //刷新列表
        ListView lv = findViewById(R.id.lv);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,people.keySet().toArray());
        lv.setAdapter(adapter);
    }

    private void checkMsg(String text) {

        //从这里判断消息类型，来执行操作，也就是回调方法
        String head = text.split(":")[0];
        String txt = text.split(":")[1];
        if (head.equals("连接")){
            people.put(txt,socket);
            Toast.makeText(context, txt+" 进入服务器", Toast.LENGTH_SHORT).show();
            checkSize();
        }else if (head.equals("断开")){
            people.remove(txt);
            Toast.makeText(context, txt+" 离开服务器", Toast.LENGTH_SHORT).show();
            checkSize();
        }


        f5();

    }

    private void checkSize() {
        if (people.size() > 0){
            findViewById(R.id.lv).setVisibility(View.VISIBLE);
            findViewById(R.id.textView8).setVisibility(View.GONE);

        }else {
            findViewById(R.id.lv).setVisibility(View.GONE);
            findViewById(R.id.textView8).setVisibility(View.VISIBLE);

        }
    }


    public static String getWifiApSSID(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(manager);
            if (configuration != null) {
                return configuration.SSID;
            }
        } catch (Exception e) {
            Log.d(TAG, "getWifiApSSID: " + e);
        }
        return "";
    }

    /*
     *判断WIFI是否可用
     */
    public static boolean isWiFiActive(Context inContext) {
        Context context = inContext.getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equals("WIFI")
                            && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        try {

            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return " 获取IP出错:" + ex.getMessage();
        }
        // return null;
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


    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {
            handler.postDelayed(this, 1 * 1000);//设置循环时间，此处是5秒

            if (mode == 1) {
                Config.IP = getLocalIpAddress(getApplicationContext());
            }

            Config.IPsum = people.size()+"";
            //刷新 ap配置信息

            if (mode == 2) {
                te1.setText("热点名称 : " + Config.ssid);
            } else if (mode == 1) {
                te1.setText("WIFI名称 : " + Config.ssid);
            } else {
                te1.setText("无连接~~");
            }

            te2.setText("密码 : " + Config.pwd);
            te3.setText("连接数 : " + Config.IPsum);
            te4.setText("IP 地址 : " + Config.IP);

        }
    };


    String fun;

    public void share(View view) {
//分享链接 服务器  借鉴qq面对面

        View v = getLayoutInflater().inflate(R.layout.dialog_qrshare, null);


        new MaterialDialog.Builder(this)
                .title("分享服务器")
                .customView(v, true)
                .positiveText("关闭")
                .show();

        fun = ("funny://dabai2017.github.io/?tip=" + Build.MODEL + "&ssid=" + Config.ssid + "&pass=" + Config.pwd +
                "&ip=" + Config.IP + "&mode=" + mode).replace(" ", "");


        Bitmap bit = QRutil.createQRCodeBitmap(fun, 700, 700, "UTF-8", "H", "1", QRutil.QRColor, QRutil.QRBackColor);
        ImageView img = v.findViewById(R.id.imageView);
        img.setImageBitmap(bit);

    }

    public void share_text(View view) {

        sendText(fun);

    }


    private void sendText(String p0) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "把服务器码发给好友");
        // 指定发送内容的类型
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, p0);
        startActivity(Intent.createChooser(sendIntent, "把服务器码发给好友"));
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

          if (Integer.parseInt(Config.IPsum) > 0){
              new MaterialDialog.Builder(this)
                      .title("提示")
                      .content("要把服务器隐藏到后台嘛？")
                      .positiveText("确认")
                      .onPositive(new MaterialDialog.SingleButtonCallback() {
                          @Override
                          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                              Intent intent = new Intent();
                              // 为Intent设置Action、Category属性
                              intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
                              intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
                              startActivity(intent);
                          }
                      })
                      .negativeText("取消")
                      .show();

          }else {

              new MaterialDialog.Builder(this)
                      .title("提示")
                      .content("要把服务器关闭嘛？")
                      .positiveText("确认")
                      .onPositive(new MaterialDialog.SingleButtonCallback() {
                          @Override
                          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                              finish();
                              System.exit(0);
                          }
                      })
                      .negativeText("取消")
                      .show();


          }


            return false;
        }
        return super.onKeyDown(keyCode, event);
    }



}
