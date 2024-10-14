package myandroid.app.hhobzic.a360pool;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chaos.view.PinView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import myandroid.app.hhobzic.a360pool.classes.ApiResponse;
import myandroid.app.hhobzic.a360pool.classes.CheckMailStatus;
import myandroid.app.hhobzic.a360pool.classes.EmailRequest;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.classes.LoginRequest;
import myandroid.app.hhobzic.a360pool.classes.OtpRequest;
import myandroid.app.hhobzic.a360pool.classes.PasswordModelClass;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassActivity extends AppCompatActivity {
    private EditText  passwordEditText, confirmPasswordEditText;
    private ProgressDialog progressDialog;
    private LoadingDialog loadingDialog;
    private ImageView seePassImageView,hidePassImageView,seePassImageView1,hidePassImageView1;
    ConstraintLayout passLayout,confirmPassLayout;
    String uid;
    Button create;
    TextInputEditText editText;
    Button button, confirmbtn;
    int code;
    PinView pinView;
    String email,pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        loadingDialog = new LoadingDialog(this, "Loading...");

        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);

        seePassImageView = findViewById(R.id.seePass);
        hidePassImageView = findViewById(R.id.hidePass);
        seePassImageView1 = findViewById(R.id.seePass1);
        hidePassImageView1 = findViewById(R.id.hidePass1);
        passLayout = findViewById(R.id.passLayout);
        confirmPassLayout = findViewById(R.id.confirmPassLayout);

        create = findViewById(R.id.registration);
        editText = findViewById(R.id.signup_Email);
        button = findViewById(R.id.signup_btn);
        pinView = findViewById(R.id.firstPinView);
        confirmbtn = findViewById(R.id.signup_confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyEmail();
            }
        });

        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText.getText().toString();
                String otp = pinView.getText().toString();
                verifyOtp(email, otp);

            }
        });
        seePassImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show password
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                seePassImageView.setVisibility(View.GONE);
                hidePassImageView.setVisibility(View.VISIBLE);
            }
        });

        hidePassImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide password
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seePassImageView.setVisibility(View.VISIBLE);
                hidePassImageView.setVisibility(View.GONE);
            }
        });
        seePassImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show password
                confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                seePassImageView1.setVisibility(View.GONE);
                hidePassImageView1.setVisibility(View.VISIBLE);
            }
        });

        hidePassImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide password
                confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seePassImageView1.setVisibility(View.VISIBLE);
                hidePassImageView1.setVisibility(View.GONE);
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())){
                    pass=passwordEditText.getText().toString();
                    if (isPasswordStrong(pass)){
                        loadingDialog.show();
                        updatePasswordInRealtimeDatabase(email,pass);
                    }else {
                        Toast.makeText(ChangePassActivity.this, "Password did not match the requirements!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(ChangePassActivity.this, "Password not matched!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void verifyEmail() {

        loadingDialog.show();
        email=editText.getText().toString();
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<CheckMailStatus> call=apiService.mailStatus(new LoginRequest(email));
        call.enqueue(new Callback<CheckMailStatus>() {
            @Override
            public void onResponse(Call<CheckMailStatus> call, Response<CheckMailStatus> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body()!=null){
                    CheckMailStatus checkMailStatus= response.body();
                    if (checkMailStatus.getAvailable()){
                        sendOtp(email);
                    }
                    else {
                        loadingDialog.dismiss();
                        Toast.makeText(ChangePassActivity.this, checkMailStatus.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    loadingDialog.dismiss();
                    Toast.makeText(ChangePassActivity.this, response.message().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckMailStatus> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ChangePassActivity.this, "Error! "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updatePasswordInRealtimeDatabase(String email, String newPassword) {
        String safeEmail = email.replace(".", ",").replace("@", "_");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(safeEmail);
        userRef.child("password").setValue(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingDialog.dismiss();
                        Toast.makeText(ChangePassActivity.this, "Password updated.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(ChangePassActivity.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isPasswordStrong(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=.*[0-9]).{8,}$";
        return password.matches(passwordPattern);
    }

    private void sendOtp(String email) {
        loadingDialog=new LoadingDialog(this,"Sending OTP...");
        loadingDialog.show();
        EmailRequest emailRequest = new EmailRequest(email);
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<ApiResponse> call = apiService.sendOtp(emailRequest);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePassActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.LinearLayout_PinView).setVisibility(View.VISIBLE);
                    confirmbtn.setVisibility(View.VISIBLE);
                    button.setVisibility(View.GONE);
                    loadingDialog.dismiss();
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(ChangePassActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ChangePassActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void verifyOtp(String email, String otp) {
        loadingDialog=new LoadingDialog(this,"Verifying OTP...");
        loadingDialog.show();
        OtpRequest otpRequest = new OtpRequest(email, otp);
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<ApiResponse> call = apiService.verifyOtp(otpRequest);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePassActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    findViewById(R.id.LinearLayout_PinView).setVisibility(View.GONE);
                    passLayout.setVisibility(View.VISIBLE);
                    confirmPassLayout.setVisibility(View.VISIBLE);
                    create.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ChangePassActivity.this, "Failed to verify OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ChangePassActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}