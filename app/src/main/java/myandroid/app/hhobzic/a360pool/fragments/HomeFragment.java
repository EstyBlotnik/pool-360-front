package myandroid.app.hhobzic.a360pool.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import myandroid.app.hhobzic.a360pool.classes.DetectedPersons;
import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.IssueResponse;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.classes.SensorData;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.a360pool.service.FrequentTaskService;
import myandroid.app.hhobzic.a360pool.service.MyApplication;
import myandroid.app.hhobzic.pool360.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements FIxingFragment.OnFixingFragmentInteractionListener{

    private static final String CHANNEL_ID = "pool_alert_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int SENSOR_DATA_FETCH_INTERVAL_MS = 60000;
    private static final long INTERVAL_MS = 100;
    public static boolean isPoolActive;
    private static CardView bottomCard;
    private static final String IMAGE_URL = "https://esp32cam.ngrok.app/cam-hi.jpg";
    private static TextView poolActive;
    private static TextView poolClose;
    private static TextView message;
    private static ImageView expand, warningImageView;
    private static TextView waterClarityTextView, fixTextView , lastCheckTextView;
    private static MediaPlayer mediaPlayer;
    private static LinearLayout fixAll;
    private boolean isFullScreen = false;
    private LoadingDialog loadingDialog;
    private Runnable fetchSensorDataRunnable;
    private static FrameLayout frameLayout;
    float waterLevelUser;

    private float maxWaterLevel;
    private float minWaterLevel;
    private Runnable deactivatePoolRunnable = this::setPoolInactive;
    private static ImageView imageView;
    static boolean safetyAlert;
    private static final long ALARM_INTERVAL_MS = 300000; // 5 minutes
    private static final long POLLING_INTERVAL_MS = 20000; // 20 seconds
    private static final AtomicLong lastAlarmTime = new AtomicLong(0);
    private static final Handler handler = new Handler();
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully");
        }
    }


    @SuppressLint("NewApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeUI(view);
        setupListeners();
        initializeMediaPlayer();
        fetchSensorData();
        checkWaterLevel();
        /*startImageFetching();*/
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable fetchAndUpdateImage = new Runnable() {
            @Override
            public void run() {
                new FetchImageTask1(handler, this).execute(IMAGE_URL);
            }
        };
        handler.post(fetchAndUpdateImage);
        setPoolInactive();
        return view;
    }

    private void checkWaterLevel() {
        loadingDialog.show();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String localId = sharedPreferences.getString("localId", "");
        String email = sharedPreferences.getString("email", "");
        Log.d("localId",localId);

        if (localId != null && !localId.isEmpty()) {
            ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
            Call<GetUsersData> call = apiService.getUserData(email);

            call.enqueue(new Callback<GetUsersData>() {
                @Override
                public void onResponse(Call<GetUsersData> call, Response<GetUsersData> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        loadingDialog.dismiss();
                        GetUsersData userData = response.body();
                        waterLevelUser=userData.getWaterLevel();
                        safetyAlert=userData.getSafetyAlert();
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GetUsersData> call, Throwable t) {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
        }
    }

    public static void initializeUI(View view) {

        imageView = view.findViewById(R.id.imageView);
        poolActive = view.findViewById(R.id.poolActive);
        poolClose = view.findViewById(R.id.poolClose);
        message = view.findViewById(R.id.message);
        expand = view.findViewById(R.id.expand);
        lastCheckTextView = view.findViewById(R.id.currentDateandTime);
        bottomCard = view.findViewById(R.id.bottomCard);
        warningImageView = view.findViewById(R.id.warning);
        waterClarityTextView = view.findViewById(R.id.waterClarity);
        fixTextView = view.findViewById(R.id.fix);
        fixAll = view.findViewById(R.id.fixAll);
        frameLayout = view.findViewById(R.id.fragment_container);
    }

    private void setupListeners() {
        poolActive.setOnClickListener(v -> setPoolActive());
        poolClose.setOnClickListener(v -> setPoolInactive());
        expand.setOnClickListener(v -> toggleFullScreen());
        fixAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameLayout.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FIxingFragment fixingFragment = new FIxingFragment();
                fragmentTransaction.replace(R.id.fragment_container, fixingFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm); // Ensure you have an alarm sound in res/raw folder
    }

    private void fetchSensorData() {
        loadingDialog = new LoadingDialog(requireContext(), "Loading...");
        loadingDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        fetchSensorDataRunnable = new Runnable() {
            @Override
            public void run() {
                apiService.getSensorData().enqueue(new Callback<SensorData>() {
                    @Override
                    public void onResponse(@NonNull Call<SensorData> call, @NonNull Response<SensorData> response) {
                        loadingDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            SensorData sensorData = response.body();
                            updateUIBasedOnSensorData(sensorData);
                            updateDateTimeTextView();
                            Log.d("TAG", "Updated UI with values: pH=" + sensorData.getpHValue() +
                                    ", Temp=" + sensorData.getTemperature() +
                                    ", Turbidity=" + sensorData.getTurbidity() +
                                    ", Water Level=" + sensorData.getWaterLevel());
                        } else {
                            Log.d("TAG", "Response not successful or body is null");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SensorData> call, @NonNull Throwable t) {
                        loadingDialog.dismiss();
                        Toast.makeText(requireContext(), "Error fetching sensor data", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", t.getMessage(), t);
                    }
                });
                handler.postDelayed(this, SENSOR_DATA_FETCH_INTERVAL_MS);
            }
        };

        handler.post(fetchSensorDataRunnable);
    }

    private void updateDateTimeTextView() {
        String currentDateTime = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date());
        lastCheckTextView.setText("Last Check " + currentDateTime);
    }

    private void updateUIBasedOnSensorData(SensorData sensorData) {
        Double pHValue = sensorData.getpHValue();
        Double temperature = sensorData.getTemperature();
        Double turbidity = sensorData.getTurbidity();
        Double waterLevel = sensorData.getWaterLevel();
        minWaterLevel = waterLevelUser - 5.0f;
        maxWaterLevel = waterLevelUser + 5.0f;
        boolean isPHNormal = pHValue >= 7.2 && pHValue <= 7.8;
        boolean isTemperatureNormal = temperature >= 20.0 && temperature <= 30.0;
        boolean isTurbidityNormal = turbidity >= 0.5 && turbidity <= 1.0;
        boolean isWaterLevelNormal = waterLevel >= minWaterLevel && waterLevel <= maxWaterLevel;

        if (isPHNormal && isTemperatureNormal && isTurbidityNormal && isWaterLevelNormal) {
            loadingDialog.dismiss();
            warningImageView.setImageResource(R.drawable.correct);
            waterClarityTextView.setText("All Measurements are \nnormal");
            fixTextView.setText("See All");
        } else {
            loadingDialog.dismiss();
            warningImageView.setImageResource(R.drawable.warning);

            StringBuilder message = new StringBuilder();
            if (!isPHNormal) {
                message.append("pH value out of range. ");
                /*if (pHValue < 7.2) {
                    reportIssue("pH Level", "Low " + pHValue.toString(), "Status: Quality Alert!");
                } else if (pHValue > 7.8) {
                    reportIssue("pH Level", "High " + pHValue.toString(), "Status: Quality Alert!");
                }*/
            }
            if (!isTemperatureNormal) {
                message.append("Temperature out of range. ");
               /* if (temperature < 20.0) {
                    reportIssue("Temperature", "Low " + temperature.toString(), "Status: Quality Alert!");
                } else if (temperature > 30.0) {
                    reportIssue("Temperature", "High " + temperature.toString(), "Status: Quality Alert!");
                }*/
            }
            if (!isTurbidityNormal) {
                message.append("Turbidity too high. ");
                /*if (turbidity < 0.5) {
                    reportIssue("Water Clarity", "Low " + turbidity.toString(), "Status: Quality Alert!");
                } else if (turbidity > 1.0) {
                    reportIssue("Water Clarity", "High " + turbidity.toString(), "Status: Quality Alert!");
                }*/
            }
            if (!isWaterLevelNormal) {

                if (waterLevel < minWaterLevel) {
                    /*reportIssue("Water Level", "Low " + waterLevel.toString(), "Status: Quality Alert!");*/
                } else if (waterLevel > maxWaterLevel) {
                    /*reportIssue("Water Level", "High " + waterLevel.toString(), "Status: Quality Alert!");*/
                }else {
                    message.append("Water level out of range. ");
                }
            }

            waterClarityTextView.setText(message.toString().trim());
            fixTextView.setText("Fix Now");
        }
    }



    private static void reportIssue(String sensorName, String sensorValue, String s) {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

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


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*startCamera();*/

    }
    public static void  startImageFetching() {
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable fetchAndUpdateImage = new Runnable() {
            @Override
            public void run() {
                new FetchImageTask(handler, this).execute(IMAGE_URL);
            }
        };

        handler.post(fetchAndUpdateImage);
    }
    public class FetchImageTask1 extends AsyncTask<String, Void, Bitmap> {
        private Handler handler;
        private Runnable nextRun;

        public FetchImageTask1(Handler handler, Runnable nextRun) {
            this.handler = handler;
            this.nextRun = nextRun;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            handler.postDelayed(nextRun, INTERVAL_MS);
        }
    }

    public static class FetchImageTask extends AsyncTask<String, Void, Bitmap> {
        private Handler handler;
        private Runnable nextRun;

        public FetchImageTask(Handler handler, Runnable nextRun) {
            this.handler = handler;
            this.nextRun = nextRun;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                /*imageView.setImageBitmap(bitmap);*/
                if (isPoolActive) {
                    detectFaces(bitmap);
                }
            }

            handler.postDelayed(nextRun, INTERVAL_MS);
        }
    }

    public static void detectFaces(Bitmap bitmap) {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        apiService.detectPerson(new DetectedPersons(IMAGE_URL)).enqueue(new Callback<DetectedPersons>() {
            @Override
            public void onResponse(Call<DetectedPersons> call, Response<DetectedPersons> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    if ("Detection started".equals(message)) {
                        startPolling(apiService, bitmap);
                    } else {
                        Log.e("DetectionAPI", "Failed to start detection: " + message);
                    }
                } else {
                    Log.e("DetectionAPI", "Detection API call failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DetectedPersons> call, Throwable t) {
                Log.e("DetectionAPI", "Detection API call failed", t);
            }
        });
    }

    private static void startPolling(ApiService apiService, Bitmap bitmap) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                apiService.getStatus().enqueue(new Callback<DetectedPersons>() {
                    @Override
                    public void onResponse(Call<DetectedPersons> call, Response<DetectedPersons> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DetectedPersons detectionResponse = response.body();
                            if (safetyAlert){
                                handleDetectionResponse(detectionResponse, bitmap);
                            }
                        } else {
                            Log.e("StatusPolling", "Status polling failed: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<DetectedPersons> call, Throwable t) {
                        Log.e("StatusPolling", "API call failed", t);
                    }
                });

                // Poll every 20 seconds
                handler.postDelayed(this, POLLING_INTERVAL_MS);
            }
        };

        // Start polling
        handler.post(runnable);
    }

    private static void handleDetectionResponse(DetectedPersons detectionResponse, Bitmap bitmap) {
        if (detectionResponse.getStatus()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAlarmTime.get() >= ALARM_INTERVAL_MS) {
                // Trigger the alarm and notification
                Log.d("trigger", "alarm");
                String alert = "Safety Alert! A Person is detected";
                updateTextView(alert);
                String bitmapString = convertBitmapToBase64(bitmap);
                reportIssue("Pool Safety Alert", bitmapString, "Status: Safety Alert!");
                triggerAlarm(MyApplication.getInstance().getContext());
                sendNotification("Safety Alert!", "A Human Detected.");

                // Update the last alarm time
                lastAlarmTime.set(currentTime);
            } else {
                Log.d("trigger", "Skipping alarm. Less than 5 minutes since last trigger.");
            }
        } else {
            updateTextView();
        }
    }

    private static String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void toggleFullScreen() {
        if (isFullScreen) {
            showUIElements();
        } else {
            hideUIElements();
        }
        isFullScreen = !isFullScreen;
    }

    private void showUIElements() {
        poolActive.setVisibility(View.VISIBLE);
        poolClose.setVisibility(View.VISIBLE);
        message.setVisibility(View.VISIBLE);
        bottomCard.setVisibility(View.VISIBLE);
    }

    private void hideUIElements() {
        poolActive.setVisibility(View.GONE);
        poolClose.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        bottomCard.setVisibility(View.GONE);
    }

    public static void triggerAlarm(Context context) {
        if (context != null) {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
            if (mediaPlayer != null) {
                try {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }

                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "MediaPlayer is not in a valid state to start.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.e("HomeFragment", "Context is null, unable to trigger alarm.");
        }
    }


    private static void updateTextView(String alert) {
        message.setText(alert);
        message.setTextColor(MyApplication.getInstance().getContext().getResources().getColor(R.color.red));
    }
    private static void updateTextView() {
        String alert = "Pool Safe: No Todlers Present";
        message.setText(alert);
        message.setTextColor(MyApplication.getInstance().getContext().getResources().getColor(R.color.black));
    }

    private static void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.getInstance().getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.logo360)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MyApplication.getInstance().getContext());
        if (ActivityCompat.checkSelfPermission(MyApplication.getInstance().getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MyApplication.getInstance().getContext(), "Turn on Notification.", Toast.LENGTH_SHORT).show();
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pool Alert Channel";
            String description = "Channel for pool alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void setPoolActive() {
        isPoolActive = false;
        poolActive.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corners_black));
        poolClose.setBackground(null);

        poolActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.appColor));
        poolActive.setTypeface(null, Typeface.BOLD);

        poolClose.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        poolClose.setTypeface(null, Typeface.NORMAL);

        // Cancel any previous deactivate runnable
        handler.removeCallbacks(deactivatePoolRunnable);
        message.setText("Pool Safe: No Todddlers Present");

    }

    private void setPoolInactive() {
        isPoolActive = true;
        poolActive.setBackground(null);
        poolClose.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corners_black));

        poolActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        poolActive.setTypeface(null, Typeface.NORMAL);

        poolClose.setTextColor(ContextCompat.getColor(requireContext(), R.color.appColor));
        poolClose.setTypeface(null, Typeface.BOLD);

        // Cancel any previous deactivate runnable and post a new one
        handler.removeCallbacks(deactivatePoolRunnable);
        handler.postDelayed(deactivatePoolRunnable, 7200000);
    }


    @Override
    public void onFixingFragmentClosed() {
        FrameLayout frameLayout = getView().findViewById(R.id.fragment_container);
        if (frameLayout != null) {
            frameLayout.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(fetchSensorDataRunnable);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }
}
