package com.dabai.funny.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dabai.funny.R;
import com.dabai.funny.ctrls.Config;
import com.dabai.funny.utils.ApManager;
import com.dabai.funny.utils.WifiAdmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientToolActivity extends AppCompatActivity {

    private String password, netWorkType, netWorkName;
    private WifiAdmin wifiAdmin;
    private ProgressDialog wifipd;
    private WifiManager mWifiManager;
    WifiInfo mWifiInfo;
    String TAG = "dabai";
    TextView te1, te2, te4, te5;
    private StringBuffer sb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_tool);


        init();
        init_val();
        wifiAdmin = new WifiAdmin(getApplicationContext());

        apinit();

        handler.post(task);//立即调用
        msgs = findViewById(R.id.msgs);
    }

    //初始化 wifi连接
    private void apinit() {



        password = Config.pwd;
        netWorkType = "WPA";
        netWorkName = Config.ssid;

        Toast.makeText(this, "正在进行验证...", Toast.LENGTH_LONG).show();
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
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //尝试向主机发包。判断连接结果
                        checkOK();

                    }
                });

            }
        }).start();

    }

    private void checkOK() {

        sendMsg("连接:"+getLocalIpAddress(getApplicationContext()));

    }


    //发送文本
    private void sendMsg(final String text) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    //1.创建监听指定服务器地址以及指定服务器监听的端口号
                    Socket socket = new Socket(Config.HostIP, 7657);
                    //2.拿到客户端的socket对象的输出流发送给服务器数据
                    OutputStream os = socket.getOutputStream();
                    //写入要发送给服务器的数据
                    String s1 = new String(text.getBytes(),"UTF-8");
                    os.write(s1.getBytes());
                    os.flush();
                    socket.shutdownOutput();
                    //拿到socket的输入流，这里存储的是服务器返回的数据
                    InputStream is = socket.getInputStream();
                    //解析服务器返回的数据
                    int lenght = 0;
                    byte[] buff = new byte[1024];
                    final StringBuffer sb = new StringBuffer();
                    while((lenght = is.read(buff)) != -1){
                        sb.append(new String(buff,0,lenght,"UTF-8"));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //这里更新UI
                            checkRes(sb.toString());
                            //Toast.makeText(ClientToolActivity.this, "服务器返回消息:"+sb.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //3、关闭IO资源（注：实际开发中需要放到finally中）
                    is.close();
                    os.close();
                    socket.close();
                } catch (UnknownHostException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new MaterialDialog.Builder(ClientToolActivity.this)
                                        .title("提示")
                                        .content("异常 : "+ex)
                                        .positiveText("确认")
                                        .cancelable(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            } catch (Exception exc) {
                            }
                        }
                    });
                } catch (IOException e) {
                    ex = e.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new MaterialDialog.Builder(ClientToolActivity.this)
                                        .title("提示")
                                        .content("异常 : "+ex)
                                        .positiveText("确认")
                                        .cancelable(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            } catch (Exception exc) {
                            }
                        }
                    });
                }
            }
        }.start();
    }

    String ex;

    private void checkRes(String text) {
        //判断服务器返回的消息，来做相应操作

        if (text.startsWith("欢迎连接")){

           addSystemMsg(text);

            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        openLin();
                    }
                }).start();
            } catch (Exception e) {
            }

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendMsg("断开:"+getLocalIpAddress(getApplicationContext()));
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

    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {

            handler.postDelayed(this, 1 * 1000);//设置循环时间，此处是5秒
            //取得当前时间

            Config.IP = getLocalIpAddress(getApplicationContext());

            //刷新 ap配置信息

            te1.setText("服务器名称 : " + Config.ssid);
            te2.setText("密码 : " + Config.pwd);

            te4.setText("服务器 IP :"+Config.HostIP);
            te5.setText("本机 IP : " + Config.IP);

        }
    };


    private void init_val() {
        //控件实例化

        te1 = findViewById(R.id.textView);
        te2 = findViewById(R.id.textView2);

        te4 = findViewById(R.id.textView4);
        te5 = findViewById(R.id.textView5);
    }


    private void init() {
        getSupportActionBar().setElevation(0);
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("要断开与"+Config.HostIP+"的连接嘛?")
                    .positiveText("确认")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .negativeText("取消")
                    .show();

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    TextView msgs;

    public void addSystemMsg(String text){
        msgs.setText(msgs.getText().toString()+"系统 : "+text + "\n");
    }
    public void addMsg(String text){
        msgs.setText(msgs.getText().toString()+text + "\n");
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
            serverSocket = new ServerSocket(7658);
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "7658端口被占用", Toast.LENGTH_SHORT).show();
                }
            });
        }

        while (true) {

            InputStream inputStream = null;
            try {
                Socket socket = serverSocket.accept(); //开始监听5040端口
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
                        //检查来自7658的消息
                        addMsg(sb.toString());
                    }
                });
                socket.shutdownInput();
                // 关闭IO资源
                inputStream.close();
                socket.close();
            } catch (Exception e) {
                Log.d(TAG, "openLin: " + e);
            }

        }
    }


    public void client_send(View view) {
        EditText ed = findViewById(R.id.editText);
        String msg = Build.MODEL +" : "+ed.getText().toString();
        sendMsg("全体消息:"+msg);

    }
}
