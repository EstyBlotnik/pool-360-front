package myandroid.app.hhobzic.a360pool.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.IssueResponse;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.classes.SensorData;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import myandroid.app.hhobzic.pool360.databinding.FragmentMeasuresBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeasuresFragment extends Fragment implements FIxingFragment.OnFixingFragmentInteractionListener{

    private static final String TAG = "MeasuresFragment";
    private static final int PH_MAX = 20;
    private static final int TEMP_MAX = 40;
    private static final int WATER_LEVEL_MAX = 40;
    private static final int TURBIDITY_MAX = 10;

    private FragmentMeasuresBinding binding;
    private LoadingDialog loadingDialog;
    private Handler handler = new Handler();
    private Runnable dataFetchRunnable;
    private FrameLayout frameLayout;
    float waterLevelUser;
    private float maxWaterLevel;
    private float minWaterLevel;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMeasuresBinding.inflate(inflater, container, false);
        initializeSeekBars();
        waterLevel();

        binding.fixpH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment();
            }
        });
        binding.fixwaterClarity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment();
            }
        });
        binding.fixwaterlevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment();
            }
        });
        binding.fixtemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment();
            }
        });

        return binding.getRoot();
    }

    private void waterLevel() {
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
                        fetchSensorData();
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

    private void loadFragment() {
        frameLayout.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FIxingFragment fixingFragment = new FIxingFragment();
        fragmentTransaction.replace(R.id.fragment_container, fixingFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void initializeSeekBars() {
        // Set maximum values for SeekBars
        loadingDialog = new LoadingDialog(requireContext(), "Loading...");
        frameLayout = binding.fragmentContainer;
        binding.pHseekBar.setMax(PH_MAX);
        binding.waterTempSeekBar.setMax(TEMP_MAX);
        binding.waterLevelSeekBar.setMax(WATER_LEVEL_MAX);
        binding.waterClearitySeekBar.setMax(TURBIDITY_MAX);

    }

    private void fetchSensorData() {

        loadingDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        dataFetchRunnable = new Runnable() {
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
                            Log.d(TAG, "Updated UI with values: pH=" + sensorData.getpHValue() +
                                    ", Temp=" + sensorData.getTemperature() +
                                    ", Turbidity=" + sensorData.getTurbidity() +
                                    ", Water Level=" + sensorData.getWaterLevel());
                        } else {
                            Log.d(TAG, "Response not successful or body is null");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SensorData> call, @NonNull Throwable t) {
                        loadingDialog.dismiss();
                        Toast.makeText(requireContext(), "Error fetching sensor data", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, t.getMessage(), t);
                    }
                });
                handler.postDelayed(this, 120000);
            }
        };

        handler.post(dataFetchRunnable);
    }

    private void updateUIBasedOnSensorData(SensorData sensorData) {
        float pHValue = sensorData.getpHValue() != null ? sensorData.getpHValue().floatValue() : 0;
        float temperature = sensorData.getTemperature() != null ? sensorData.getTemperature().floatValue() : 0;
        float turbidity = sensorData.getTurbidity() != null ? sensorData.getTurbidity().floatValue() : 0;
        float waterLevel = sensorData.getWaterLevel() != null ? sensorData.getWaterLevel().floatValue() : 0;

        // Update SeekBars
        binding.pHseekBar.setProgress((int) pHValue);
        binding.waterTempSeekBar.setProgress((int) temperature);
        binding.waterLevelSeekBar.setProgress((int) waterLevel);
        binding.waterClearitySeekBar.setProgress((int) turbidity);

        // Update pH Value UI
        updateTextView(binding.textpHLevel, pHValue, 7.2f, 7.8f, "pH Level");

        // Update Temperature UI
        updateTextView(binding.textWaterTemp, temperature, 15.0f, 30.0f, "Temperature");

        // Define the acceptable water level range
        minWaterLevel = waterLevelUser - 5.0f;
        maxWaterLevel = waterLevelUser + 5.0f;

        // Update Water Level UI
        updateTextView(binding.textWaterLevel, waterLevel, minWaterLevel, maxWaterLevel, "Water Level");

        // Update Turbidity UI
        updateTextView(binding.textWaterClarity, turbidity, 0.5f, 1.0f, "Water Clarity");
    }


    private void updateTextView(final View textView, final float value, final float minThreshold, final float maxThreshold, final String label) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String status;
                Drawable thumbDrawable;
                int thumbTint;

                if (value >= minThreshold && value <= maxThreshold) {
                    status = "Normal " + value;
                    changeText(label, 1);
                    thumbDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.seekbaricon);
                    thumbTint = ContextCompat.getColor(requireContext(), R.color.appColor);

                    if (label.equals("Water Level")) {
                        // If value is within the overall threshold but outside the user-defined acceptable range
                        if (value > maxWaterLevel) {
                            status = "High " + value;
                            reportIssue(label, "Water Level is High", "Status: Quality Alert!");
                            thumbDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.seekbariconpink);
                            thumbTint = ContextCompat.getColor(requireContext(), R.color.red);
                            changeText(label, 3);
                        } else if (value < minWaterLevel) {
                            status = "Low " + value;
                            reportIssue(label, "Water Level is Low", "Status: Quality Alert!");
                            thumbDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.seekbariconpink);
                            thumbTint = ContextCompat.getColor(requireContext(), R.color.red);
                            changeText(label, 2);
                        }
                    }

                } else if (value < minThreshold) {
                    status = "Low " + value;
                    changeText(label, 2);
                    reportIssue(label, status, "Status: Quality Alert!");
                    thumbDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.seekbariconpink);
                    thumbTint = ContextCompat.getColor(requireContext(), R.color.red);
                } else {
                    status = "High " + value;
                    changeText(label, 3);
                    reportIssue(label, status, "Status: Quality Alert!");
                    thumbDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.seekbariconpink);
                    thumbTint = ContextCompat.getColor(requireContext(), R.color.red);
                }

                ((TextView) textView).setText(label + ": " + status);
                SeekBar seekBar = (SeekBar) ((View) textView.getParent()).findViewById(getSeekBarId(label));
                if (seekBar == null) {
                    Log.e("UpdateTextView", "SeekBar not found for label: " + label);
                } else {
                    seekBar.setThumb(thumbDrawable);
                    DrawableCompat.setTint(thumbDrawable, thumbTint);
                }
            }
        });
    }



    private void changeText(String label, int statusCode) {
        Map<String, Integer> textMap = new HashMap<>();
        textMap.put("pH Level", R.id.textfixpH);
        textMap.put("Temperature", R.id.textfixtemperature);
        textMap.put("Water Level", R.id.textfixwaterlevel);
        textMap.put("Water Clarity", R.id.textfixwaterClarity);

        Integer textViewId = textMap.get(label);
        if (textViewId != null) {
            TextView textView = getView().findViewById(textViewId);
            if (textView != null) {
                switch (statusCode) {
                    case 1:
                        textView.setText("More");
                        break;
                    case 2:
                        textView.setText("Fix Now");
                        break;
                    case 3:
                        textView.setText("Fix Now");
                        break;
                    default:
                        Log.e("ChangeText", "Invalid status code: " + statusCode);
                        break;
                }
            } else {
                Log.e("ChangeText", "TextView not found for label: " + label);
            }
        } else {
            Log.e("ChangeText", "No matching TextView for label: " + label);
        }
    }


    private void reportIssue(String sensorName, String sensorValue, String s) {
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

    private int getSeekBarId(String label) {
        switch (label) {
            case "pH Level":
                return R.id.pHseekBar;
            case "Temperature":
                return R.id.waterTempSeekBar;
            case "Water Level":
                return R.id.waterLevelSeekBar;
            case "Water Clarity":
                return R.id.waterClearitySeekBar;
            default:
                throw new IllegalArgumentException("Unknown label: " + label);
        }
    }

    private void updateDateTimeTextView() {
        String currentDateTime = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date());
        binding.currentDateandTime.setText("Last Check " + currentDateTime);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && dataFetchRunnable != null) {
            handler.removeCallbacks(dataFetchRunnable);
        }
        binding = null;
    }

    @Override
    public void onFixingFragmentClosed() {
        FrameLayout frameLayout = getView().findViewById(R.id.fragment_container);
        if (frameLayout != null) {
            frameLayout.setVisibility(View.GONE);
        }
    }
}
