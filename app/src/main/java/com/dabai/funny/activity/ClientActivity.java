package com.dabai.funny.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dabai.funny.R;
import com.dabai.funny.ctrls.Config;

public class ClientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        init();


        try {
            Intent intent = getIntent();

            if (intent != null)
            {
                String url = intent.getData().toString();

                String result = url;
                if (result.contains("funny") && result.contains("ssid") && result.contains("ip") && result.contains("pass")) {

                    Config.ssid = result.split("/")[3].split("&")[1].split("=")[1];
                    Config.pwd = result.split("/")[3].split("&")[2].split("=")[1];
                    Config.IP = result.split("/")[3].split("&")[3].split("=")[1];

                    if (result.contains("funny") && result.contains("ssid")) {
                        finish();
                        Intent intent2 = new Intent(this, ClientToolActivity.class);
                        startActivity(intent2);
                    }

                } else {
                    Toast.makeText(this, "滑稽控:此链接不是合法的!", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        } catch (Exception e) {
            //Toast.makeText(this, "滑稽控:出错了！"+e, Toast.LENGTH_SHORT).show();
        }


    }


    private void init() {
        getSupportActionBar().setElevation(0);
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public void scanning(View view) {
        startActivity(new Intent(this,ClientScanActivity.class));
    }

    public void textlink(View view) {
        startActivity(new Intent(this,ClientLinkActivity.class));
    }
}
