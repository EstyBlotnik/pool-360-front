package myandroid.app.hhobzic.a360pool;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import myandroid.app.hhobzic.a360pool.classes.SignupRequest;
import myandroid.app.hhobzic.a360pool.classes.SignupResponse;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_PERMISSION = 100;

    private EditText firstNameEditText, familyNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private ProgressDialog progressDialog;
    private TextView signIn;
    private ImageView seePassImageView,hidePassImageView,seePassImageView1,hidePassImageView1;
    private RoundedImageView selectFromGallery;
    private String imageBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firstNameEditText = findViewById(R.id.fistName);
        familyNameEditText = findViewById(R.id.familyName);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        signIn = findViewById(R.id.signIn);
        seePassImageView = findViewById(R.id.seePass);
        hidePassImageView = findViewById(R.id.hidePass);
        seePassImageView1 = findViewById(R.id.seePass1);
        hidePassImageView1 = findViewById(R.id.hidePass1);
        selectFromGallery = findViewById(R.id.selectFromGallery);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing up...");
        progressDialog.setCancelable(false);

        findViewById(R.id.registration).setOnClickListener(v -> signUp());

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        selectFromGallery.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RegistrationActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
            } else {
                pickImageFromGallery();
            }
        });

    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageBase64 = encodeImageToBase64(bitmap);
                selectFromGallery.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    private void signUp() {

        String firstName = firstNameEditText.getText().toString().trim();
        String familyName = familyNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (firstName.isEmpty() || familyName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordStrong(password)) {
            Toast.makeText(this, "Password must contain at least 1 uppercase letter, 1 special character, and 1 number", Toast.LENGTH_SHORT).show();
            return;
        }

        SignupRequest signupRequest = new SignupRequest(firstName, familyName, email, password,imageBase64,0.0F,false,false);

        signupRequest.setUserName(firstName);
        signupRequest.setFullName(familyName);
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setImage(imageBase64);
        signupRequest.setWaterLevel(0.0F);
        signupRequest.setSafetyAlert(false);
        signupRequest.setQualityAlert(false);

        progressDialog.show();

        ApiService apiInterface = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<SignupResponse> call = apiInterface.signup(signupRequest);
        call.enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    SignupResponse signupResponse = response.body();
                    if (signupResponse != null) {
                        /*Toast.makeText(RegistrationActivity.this, signupResponse.getMessage(), Toast.LENGTH_SHORT).show();*/
                        createFirebaseUser(email, password);
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this, "Failed to signup", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(RegistrationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("error", t.getMessage().toString());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isPasswordStrong(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=.*[0-9]).{8,}$";
        return password.matches(passwordPattern);
    }
    private void createFirebaseUser(String email, String password) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Send email verification
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(RegistrationActivity.this, "Check your email before login!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(RegistrationActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
