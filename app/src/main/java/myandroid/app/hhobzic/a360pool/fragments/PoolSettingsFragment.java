package myandroid.app.hhobzic.a360pool.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.classes.RequestAlerts;
import myandroid.app.hhobzic.a360pool.classes.RequestWaterLevel;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import myandroid.app.hhobzic.pool360.databinding.FragmentPoolSettingsBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PoolSettingsFragment extends Fragment {

    private FragmentPoolSettingsBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPoolSettingsBinding.inflate(inflater, container, false);
        initlialize();
        loadUserData();
        click();

        return binding.getRoot();
    }

    private void click() {
        binding.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogue();
            }
        });
        binding.qualityAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.safetyAlert.isChecked()){
                updateAlerts(true, true);
            }else {
                updateAlerts(true, false);
            }
        });

        binding.safetyAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.qualityAlert.isChecked()){
                updateAlerts(true, true);
            }else {
                updateAlerts(false, true);
            }
        });
    }

    private void updateAlerts(boolean qualityAlertStatus, boolean safetyAlertStatus) {
        loadingDialog.show();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String localId = sharedPreferences.getString("localId", "");
        String email = sharedPreferences.getString("email", "");

        RequestAlerts requestAlerts = new RequestAlerts();
        requestAlerts.setUid(email);
        requestAlerts.setQualityAlert(qualityAlertStatus);
        requestAlerts.setSafetyAlert(safetyAlertStatus);

        ApiService apiInterface = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = apiInterface.updateAlerts(requestAlerts);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Alerts updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to update alerts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openDialogue() {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue_water_level, null);

        EditText dialogEditText = dialogView.findViewById(R.id.dialog_edittext);
        Button buttonCancel = dialogView.findViewById(R.id.dialog_button_cancel);
        Button buttonOk = dialogView.findViewById(R.id.dialog_button_ok);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = dialogEditText.getText().toString();
                saveWaterLevel(inputText,dialog);
            }
        });

        dialog.show();
    }

    private void saveWaterLevel(String water, AlertDialog dialog) {
        loadingDialog.show();
        float waterLevel= Float.parseFloat(water);
        if (waterLevel==0.0F){
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Please Enter Water Level", Toast.LENGTH_SHORT).show();
        } else {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String localId = sharedPreferences.getString("localId", "");
            String email = sharedPreferences.getString("email", "");
            RequestWaterLevel requestWaterLevel=new RequestWaterLevel(String.valueOf(waterLevel),localId);
            requestWaterLevel.setWaterLevel(String.valueOf(waterLevel));
            requestWaterLevel.setUid(email);
            ApiService apiInterface = ApiClient.getRetrofitInstance().create(ApiService.class);
            Call<Void> call = apiInterface.saveWaterLevel(requestWaterLevel);
            Log.e("error",call.toString());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    loadingDialog.dismiss();
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Water Level Saved!", Toast.LENGTH_SHORT).show();
                    loadUserData();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void initlialize() {
        loadingDialog = new LoadingDialog(requireContext(), "Loading...");
    }

    private void loadUserData() {

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
                        binding.qualityAlert.setChecked(userData.getQualityAlert());
                        binding.safetyAlert.setChecked(userData.getSafetyAlert());
                        binding.waterLevel.setText("Desired Pool Height: "+ String.valueOf(userData.getWaterLevel()));

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
