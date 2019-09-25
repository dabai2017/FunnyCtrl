package com.dabai.funny.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.dabai.funny.R;

public class ClientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        init();
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
