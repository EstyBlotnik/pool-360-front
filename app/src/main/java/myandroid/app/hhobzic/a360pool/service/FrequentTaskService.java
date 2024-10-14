package myandroid.app.hhobzic.a360pool.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import myandroid.app.hhobzic.pool360.R;

public class FrequentTaskService extends Service {

    private static final String CHANNEL_ID = "pool_alert_channel";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        scheduleSensorCheckWorker();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sensor Check Service")
                .setContentText("Running sensor checks every 2 minutes")
                .setSmallIcon(R.drawable.logo360) // Replace with your notification icon
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set to HIGH for sound and pop-up
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Enables sound and vibration
                .build();

        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        WorkManager.getInstance(this).cancelAllWork();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sensor Check Notifications",
                    NotificationManager.IMPORTANCE_HIGH // Use HIGH to ensure sound and vibration
            );
            channel.setDescription("Notifications for sensor checks");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 1000});

            // Set custom sound

            Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
            channel.setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void scheduleSensorCheckWorker() {
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(SensorCheckWorker.class, 2, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SensorCheckWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
        );
    }

}
