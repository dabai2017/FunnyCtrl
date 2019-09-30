package com.dabai.funny.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dabai.funny.R;
import com.dabai.funny.ctrls.Config;
import com.google.android.material.textfield.TextInputEditText;

public class ClientLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_link);
        init();
    }

    private void init() {
        getSupportActionBar().setElevation(0);
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }


    public void cliplink(View view) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String result = clipboardManager.getText().toString();
        if (result.contains("funny") && result.contains("ssid")) {

            Config.ssid = result.split("/")[3].split("&")[1].split("=")[1];
            Config.pwd = result.split("/")[3].split("&")[2].split("=")[1];
            Config.HostIP = result.split("/")[3].split("&")[3].split("=")[1];

            if (result.contains("funny") && result.contains("ssid")) {
                finish();
                Intent intent = new Intent(this, ClientToolActivity.class);
                startActivity(intent);
            }

        } else {
            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("剪切板不包括服务器地址!")
                    .positiveText("确认")
                    .show();
        }
    }

    public void linklink(View view) {

        TextInputEditText tie = findViewById(R.id.tie1);
        String result = tie.getText().toString();
        if (result.contains("funny") && result.contains("ssid")) {

            Config.ssid = result.split("/")[3].split("&")[1].split("=")[1];
            Config.pwd = result.split("/")[3].split("&")[2].split("=")[1];
            Config.HostIP = result.split("/")[3].split("&")[3].split("=")[1];

            if (result.contains("funny") && result.contains("ssid")) {
                finish();
                Intent intent = new Intent(this, ClientToolActivity.class);
                startActivity(intent);
            }

        } else {
            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("输入的不包括服务器地址!")
                    .positiveText("确认")
                    .show();
        }

    }
}
