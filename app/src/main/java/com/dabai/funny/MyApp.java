package com.dabai.funny;

import android.app.Application;
import android.content.Context;


/**
 * Created by 。 on 2018/12/5.
 */

public class MyApp extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

    }
    public static Context getContext(){
        return context;
    }



}