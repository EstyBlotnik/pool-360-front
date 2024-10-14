package myandroid.app.hhobzic.a360pool.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.classes.SensorData;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import myandroid.app.hhobzic.pool360.databinding.FragmentFIxingBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FIxingFragment extends Fragment {
    private LoadingDialog loadingDialog;
    private FragmentFIxingBinding binding;
    private static final String TAG = "FixingFragment";
    public interface OnFixingFragmentInteractionListener {
        void onFixingFragmentClosed();
    }
    float waterLevelUser;
    private float maxWaterLevel;
    private float minWaterLevel;
    private OnFixingFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentFIxingBinding.inflate(inflater, container, false);
        loadingDialog = new LoadingDialog(requireContext(), "Loading...");
        waterLevel();

        binding.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchSensorData();
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

    private void fetchSensorData() {

        loadingDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

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
    }

    private void updateUIBasedOnSensorData(SensorData sensorData) {
        float pHValue = sensorData.getpHValue() != null ? sensorData.getpHValue().floatValue() : 0;
        float temperature = sensorData.getTemperature() != null ? sensorData.getTemperature().floatValue() : 0;
        float turbidity = sensorData.getTurbidity() != null ? sensorData.getTurbidity().floatValue() : 0;
        float waterLevel = sensorData.getWaterLevel() != null ? sensorData.getWaterLevel().floatValue() : 0;
        minWaterLevel = waterLevelUser - 5.0f;
        maxWaterLevel = waterLevelUser + 5.0f;
        // Update pH Value UI
        updateTextView(binding.sensorValueTextView, pHValue, 7.2f, 7.8f, "pH Level");

        // Update Temperature UI
        updateTextView(binding.waterTemperatureValueTextView, temperature, 15.0f, 30.0f, "Temperature");

        // Update Water Level UI
        updateTextView(binding.waterLevelValueTextView, waterLevel, minWaterLevel, maxWaterLevel, "Water Level");

        // Update Turbidity UI
        updateTextView(binding.turbidityValueTextView, turbidity, 0.5f, 1.0f, "Water Clarity");
    }

    private void updateTextView(View textView, float value, float minThreshold, float maxThreshold, String label) {
        String status;
        int color;

        if (value >= minThreshold && value <= maxThreshold) {
            status = "Normal " + value;
            color = ContextCompat.getColor(requireContext(), R.color.green);

            if (label.equals("pH Level")) {
                binding.fixedphLevel.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fixed));
                binding.sensorValueTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                binding.issuephLevel.setText("pH Level is fine");
                String message = "pH Level is Normal";
                binding.donephLevel.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "pH Level", message, color));

            } else if (label.equals("Temperature")) {
                binding.fixedTemperature.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fixed));
                binding.waterTemperatureValueTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                binding.issueTemperature.setText("Temperature is fine");
                String message = "Temperature is Normal";
                binding.doneTemperature.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Temperature", message, color));

            } else if (label.equals("Water Level")) {
                binding.fixedWaterLevel.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fixed));
                binding.waterLevelValueTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                binding.issueWaterLevel.setText("Water Level is fine");
                String message = "Water Level is Normal";
                binding.doneWaterLevel.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Water Level", message, color));

            } else if (label.equals("Water Clarity")) {
                binding.fixedTurbidity.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fixed));
                binding.turbidityValueTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                binding.issueTurbidity.setText("Water is clear");
                String message = "Water Clarity is Normal";
                binding.doneTurbidity.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Water Clarity", message, color));
            }

        } else if (value < minThreshold) {
            status = "Low " + value;
            color = ContextCompat.getColor(requireContext(), R.color.red);

            if (label.equals("pH Level")) {
                binding.fixedphLevel.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.sensorValueTextView.setTextColor(color);
                binding.issuephLevel.setText("Add Chemicals to fix pH Level");
                String message = "Adding basic material: Add basic material to the pool (such as sodium bicarbonate) according to " +
                        "the instructions on the package. Start with a small amount, then check the pH again after a few " +
                        "hours.\nDaily test: Continue to test the pH every two days until the level stabilizes between 7.2 and 7.6.";
                binding.donephLevel.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "pH Level", message));

            } else if (label.equals("Temperature")) {
                binding.fixedTemperature.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.waterTemperatureValueTextView.setTextColor(color);
                binding.issueTemperature.setText("Reduce the Heat");
                String message = "Heating the pool: use the pool heater or heat pump to raise the temperature to a comfortable " +
                        "level.\nCovering the pool at night: Covering the pool at night can help retain heat and reduce heat loss.";
                binding.doneTemperature.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Temperature", message));

            } else if (label.equals("Water Level")) {
                binding.fixedWaterLevel.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.waterLevelValueTextView.setTextColor(color);
                binding.issueWaterLevel.setText("Add Water");
                String message = "Adding water: Add water to the pool until the level stabilizes at the correct height (usually in the " +
                        "middle of the opening of the skimmer system).\nChecking the filling system: Make sure the automatic filling system (if present) is working " +
                        "properly";
                binding.doneWaterLevel.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Water Level", message));

            } else if (label.equals("Water Clarity")) {
                binding.fixedTurbidity.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.turbidityValueTextView.setTextColor(color);
                binding.issueTurbidity.setText("Water is not clear");
                String message = "Checking the chlorine level: Make sure the chlorine level remains between 1 and 3 ppm.\n" +
                        "Keeping the filters clean: Even if the water looks clear, keeping the filters clean is critical to " +
                        "preventing dirt build-up in the future";
                binding.doneTurbidity.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Water Clarity", message));
            }

        } else {
            status = "High " + value;
            color = ContextCompat.getColor(requireContext(), R.color.red);

            if (label.equals("pH Level")) {
                binding.fixedphLevel.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.sensorValueTextView.setTextColor(color);
                binding.issuephLevel.setText("pH Level is high");
                String message = "Adding pool acid: Add pool acid (such as hydrochloric acid) according to the instructions on the " +
                        "package. Start with a small amount, then check the pH again after a few hours.";
                binding.donephLevel.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "pH Level", message));

            } else if (label.equals("Temperature")) {
                binding.fixedTemperature.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.waterTemperatureValueTextView.setTextColor(color);
                binding.issueTemperature.setText("Temperature is High");
                String message = "Shading the pool: install canopies or shade nets over the pool to reduce the temperature.\n" +
                        "Reducing use of the pool heater: If you have a pool heater, set it to a lower temperature or turn " +
                        "it off if not needed.\nAdding cold water: Add cold water to the pool carefully to lower the temperature.";
                binding.doneTemperature.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Temperature", message));

            } else if (label.equals("Water Level")) {
                binding.fixedWaterLevel.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.waterLevelValueTextView.setTextColor(color);
                binding.issueWaterLevel.setText("Water Level is high");
                String message = "Water pumping: If the water level is too high, use a pump to pump the excess water and release " +
                        "it into a suitable drainage system.\n Drainage System Check: Make sure the pool's drainage system is working properly.";
                binding.doneWaterLevel.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Water Level", message));

            } else if (label.equals("Water Clarity")) {
                binding.fixedTurbidity.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.fix));
                binding.turbidityValueTextView.setTextColor(color);
                binding.issueTurbidity.setText("Add Chlorine");
                String message = "Checking filters: check the condition of the filters and clean them if necessary.\n" +
                        " Adding chlorine: Add chlorine to the pool according to the recommended levels.";
                binding.doneTurbidity.setOnClickListener(v -> showIssueDetailsDialog(binding.dateTimeTextView.getText().toString(), "Water Clarity", message));
            }
        }

        // Set text with value and status
        ((TextView) textView).setText(label + ": " + value + "\nStatus: " + status);
        ((TextView) textView).setTextColor(color);
    }
    private void showIssueDetailsDialog(String dateTime, String sensorName, String message, int color) {

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_issue_details, null);

        TextView dialogDateTimeTextView = dialogView.findViewById(R.id.dialog_dateTimeTextView);
        TextView dialogSensorNameTextView = dialogView.findViewById(R.id.dialog_sensorNameTextView);
        TextView dialogSensorValueTextView = dialogView.findViewById(R.id.dialog_sensorValueTextView);
        dialogSensorValueTextView.setTextColor(color);
        ImageView humanImage = dialogView.findViewById(R.id.humanImage);
        Button dialogOkButton = dialogView.findViewById(R.id.dialog_ok_button);


        dialogDateTimeTextView.setText(dateTime);
        dialogSensorNameTextView.setText(sensorName);
        dialogSensorValueTextView.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialogOkButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private void showIssueDetailsDialog(String dateTime, String sensorName, String message) {

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_issue_details, null);

        TextView dialogDateTimeTextView = dialogView.findViewById(R.id.dialog_dateTimeTextView);
        TextView dialogSensorNameTextView = dialogView.findViewById(R.id.dialog_sensorNameTextView);
        TextView dialogSensorValueTextView = dialogView.findViewById(R.id.dialog_sensorValueTextView);
        ImageView humanImage = dialogView.findViewById(R.id.humanImage);
        Button dialogOkButton = dialogView.findViewById(R.id.dialog_ok_button);


        dialogDateTimeTextView.setText(dateTime);
        dialogSensorNameTextView.setText(sensorName);
        dialogSensorValueTextView.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialogOkButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private void updateDateTimeTextView() {
        String currentDateTime = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date());
        binding.dateTimeTextView.setText("Last Check " + currentDateTime);
        binding.dateTimeTextView1.setText("Last Check " + currentDateTime);
        binding.dateTimeTextView2.setText("Last Check " + currentDateTime);
        binding.dateTimeTextView3.setText("Last Check " + currentDateTime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFixingFragmentInteractionListener) {
            mListener = (OnFixingFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFixingFragmentInteractionListener");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up binding reference
        if (mListener != null) {
            mListener.onFixingFragmentClosed();
        }
    }
}
