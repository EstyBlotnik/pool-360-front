package myandroid.app.hhobzic.a360pool.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import myandroid.app.hhobzic.a360pool.ChangePassActivity;
import myandroid.app.hhobzic.a360pool.HomeActivity;
import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import myandroid.app.hhobzic.pool360.databinding.FragmentAccountBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private LoadingDialog loadingDialog;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        loadUserData();

        binding.changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(requireActivity(), ChangePassActivity.class);
                startActivity(intent);
            }
        });

        binding.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });

        return binding.getRoot();

    }

    private void editProfile() {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);

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
                updateUserName(inputText);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateUserName(String userName) {
        if (userName == null){
            Toast.makeText(requireContext(), "Please Enter Water Level", Toast.LENGTH_SHORT).show();
        } else {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String localId = sharedPreferences.getString("localId", "");
            String email = sharedPreferences.getString("email", "");
            loadingDialog.show();
            GetUsersData getUsersData=new GetUsersData();
            getUsersData.setUserName(userName);
            getUsersData.setEmail(email);
            ApiService apiInterface = ApiClient.getRetrofitInstance().create(ApiService.class);
            Call<Void> call = apiInterface.updateUserName(getUsersData);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void loadUserData() {
        loadingDialog = new LoadingDialog(requireContext(), "Loading...");
        loadingDialog.show();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String localId = sharedPreferences.getString("localId", "");
        String email = sharedPreferences.getString("email", "");
        if (localId != null && !localId.isEmpty()) {
            ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
            Call<GetUsersData> call = apiService.getUserData(email);

            call.enqueue(new Callback<GetUsersData>() {
                @Override
                public void onResponse(Call<GetUsersData> call, Response<GetUsersData> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        loadingDialog.dismiss();
                        GetUsersData userData = response.body();

                        binding.userName.setText(userData.getUserName());
                        binding.email.setText(userData.getEmail());
                        String profileImageBase64 = userData.getProfileImage();

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty())
                        {
                            Bitmap decodedByte = base64ToBitmap(profileImageBase64);
                            binding.userImage.setImageBitmap(decodedByte);
                        }
                        else {
                            Toast.makeText(requireContext(), "No Image Found!", Toast.LENGTH_SHORT).show();
                        }

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
            Toast.makeText(requireContext(), "No user data found", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
