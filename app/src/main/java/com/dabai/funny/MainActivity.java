package com.dabai.funny;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dabai.funny.activity.ClientActivity;
import com.dabai.funny.activity.FeedBack;
import com.dabai.funny.activity.ServiceActivity;

public class MainActivity extends AppCompatActivity {
    private String TAG = "dabai";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init() {
        getSupportActionBar().setElevation(0);
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public void client(View view) {
        startActivity(new Intent(this, ClientActivity.class));
    }

    public void service(View view) {
        startActivity(new Intent(this, ServiceActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.feedback:

                startActivity(new Intent(this, FeedBack.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}
