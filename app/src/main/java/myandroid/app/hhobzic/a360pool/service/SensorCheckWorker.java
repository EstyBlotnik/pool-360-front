package myandroid.app.hhobzic.a360pool.service;

import static android.content.Context.MODE_PRIVATE;

import static myandroid.app.hhobzic.a360pool.fragments.HomeFragment.isPoolActive;
import static myandroid.app.hhobzic.a360pool.fragments.HomeFragment.startImageFetching;
import static myandroid.app.hhobzic.a360pool.fragments.HomeFragment.triggerAlarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import myandroid.app.hhobzic.a360pool.classes.DetectedPersons;
import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.IssueResponse;
import myandroid.app.hhobzic.a360pool.classes.SensorData;
import myandroid.app.hhobzic.a360pool.fragments.HomeFragment;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SensorCheckWorker extends Worker {
    float waterLevelUser;
    boolean safetyAlert, qualityAlert;
    private static final String IMAGE_URL = "https://esp32cam.ngrok.app/cam-hi.jpg";
    private float maxWaterLevel;
    private float minWaterLevel;
    public SensorCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    private static final long INTERVAL_MS = 100;

    @NonNull
    @Override
    public Result doWork() {
        waterLevel();
        return Result.success();
    }

    public void waterLevel() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String localId = sharedPreferences.getString("localId", "");
        String email = sharedPreferences.getString("email", "");
        Log.d("localId", localId);

        if (localId != null && !localId.isEmpty()) {
            ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
            Call<GetUsersData> call = apiService.getUserData(email);

            call.enqueue(new Callback<GetUsersData>() {
                @Override
                public void onResponse(Call<GetUsersData> call, Response<GetUsersData> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GetUsersData userData = response.body();
                        waterLevelUser = userData.getWaterLevel();
                        safetyAlert = userData.getSafetyAlert();
                        qualityAlert = userData.getQualityAlert();
                        if (qualityAlert) {
                            reportissues();
                        }
                        startImageFetching();
                    } else {
                        // Handle failure
                    }
                }

                @Override
                public void onFailure(Call<GetUsersData> call, Throwable t) {
                    // Handle failure
                }
            });
        } else {
            // Handle failure
        }
    }
    public void reportissues() {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // Fetch sensor data
        apiService.getSensorData().enqueue(new Callback<SensorData>() {
            @Override
            public void onResponse(@NonNull Call<SensorData> call, @NonNull Response<SensorData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SensorData sensorData = response.body();
                    checkAndReportIssues(sensorData);
                } else {
                    Log.d("TAG", "Failed to fetch sensor data: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SensorData> call, @NonNull Throwable t) {
                Log.e("TAG", "Error fetching sensor data", t);
            }
        });
    }

    private void checkAndReportIssues(SensorData sensorData) {
        Double waterLevel = sensorData.getWaterLevel();
        minWaterLevel = waterLevelUser - 5.0f;
        maxWaterLevel = waterLevelUser + 5.0f;

        float pHValue = sensorData.getpHValue() != null ? sensorData.getpHValue().floatValue() : 0;
        float temperature = sensorData.getTemperature() != null ? sensorData.getTemperature().floatValue() : 0;
        float turbidity = sensorData.getTurbidity() != null ? sensorData.getTurbidity().floatValue() : 0;
        boolean isWaterLevelNormal = waterLevel >= minWaterLevel && waterLevel <= maxWaterLevel;

        if (pHValue < 7.2f || pHValue > 7.8f) {
            reportIssue("pH Level", pHValue < 7.2f ? "Low " + pHValue : "High " + pHValue, "Status: Quality Alert!");
        }
        if (temperature < 15.0f || temperature > 30.0f) {
            reportIssue("Temperature", temperature < 15.0f ? "Low " + temperature : "High " + temperature, "Status: Quality Alert!");
        }
        if (turbidity < 0.5f || turbidity > 1.0f) {
            reportIssue("Water Clarity", turbidity < 0.5f ? "Low " + turbidity : "High " + turbidity, "Status: Quality Alert!");
        }

        if (!isWaterLevelNormal) {
            if (waterLevel < minWaterLevel) {
                reportIssue("Water Level", "Low " + waterLevel.toString(), "Status: Quality Alert!");
            } else if (waterLevel > maxWaterLevel) {
                reportIssue("Water Level", "High " + waterLevel.toString(), "Status: Quality Alert!");
            }else {

            }
        }
    }

    private void reportIssue(String sensorName, String sensorValue, String s) {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // Create the request body
        IssueResponse issueResponse = new IssueResponse();
        issueResponse.setSensorName(sensorName);
        issueResponse.setSensorValue(sensorValue);
        issueResponse.setIssue_Title(s);
        issueResponse.setCurrentDateTime(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date()));

        // Send POST request
        apiService.saveIssue(issueResponse).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("TAG", "Issue reported successfully");
                    showNotification("Quality Alert!",sensorName, sensorValue);
                } else {
                    Log.d("TAG", "Failed to report issue: " + response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("TAG", "Error reporting issue", t);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title,String sensorName, String sensorValue) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.logo360)
                .setContentTitle("Quality Alert!")
                .setContentText(sensorName + " is " + sensorValue)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());
    }
}
