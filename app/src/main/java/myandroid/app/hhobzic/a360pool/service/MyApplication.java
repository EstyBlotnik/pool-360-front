package myandroid.app.hhobzic.a360pool.service;


import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import androidx.work.Configuration;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import myandroid.app.hhobzic.pool360.R;

public class MyApplication extends Application {

    private static MyApplication instance;
    public static final String CHANNEL_ID = "pool_alert_channel";
    private static View currentView;

    public static MyApplication getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public Context getContext() {
        return getApplicationContext();
    }
    public void setCurrentView(View view) {
        currentView = view;
    }
    public View getCurrentView() {
        return currentView;
    }
}
