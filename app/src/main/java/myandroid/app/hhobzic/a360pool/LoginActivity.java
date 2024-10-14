package myandroid.app.hhobzic.a360pool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.classes.LoginRequest;
import myandroid.app.hhobzic.a360pool.classes.LoginResponse;
import myandroid.app.hhobzic.a360pool.classes.SignupRequest;
import myandroid.app.hhobzic.a360pool.classes.SignupResponse;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private LoadingDialog loadingDialog;
    private GoogleApiClient googleApiClient;
    private ImageView seePassImageView;
    private ImageView hidePassImageView;
    String emails;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        loadingDialog = new LoadingDialog(this, "Logging in...");

        if (isLoggedIn) {
            navigateToMainActivity();
        }

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.getStarted);
        seePassImageView = findViewById(R.id.seePass);
        hidePassImageView = findViewById(R.id.hidePass);




        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        TextView createAccount = findViewById(R.id.createAccount);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, ChangePassActivity.class);
                startActivity(intent);
            }
        });
        ImageView imageView = findViewById(R.id.googleButton);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerWithGoogle();
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

    }

    private void registerWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();

        GoogleSignIn.getClient(this, gso).revokeAccess().addOnCompleteListener(task -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String username = account.getDisplayName();
            String email = account.getEmail();

            Log.d("GoogleSignIn", "Username: " + username + ", Email: " + email);
            firebaseAuthWithGoogle(account);
        } else {
            Log.e("Error",result.getStatus().toString());
            Toast.makeText(this, "Google Sign In failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        String email = acct.getEmail();
        String safeEmail = email.replace(".", ",").replace("@", "_");

        // Fetch user data from Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.child(safeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User exists, perform login
                    String password = dataSnapshot.child("password").getValue(String.class);
                    Log.d("LoginActivity", "Password:"+password);
                    if (password != null) {
                        performLogin(email, password);
                    } else {
                        Toast.makeText(LoginActivity.this, "Password not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User does not exist, create a new user in Firebase Realtime Database
                    String displayName = acct.getDisplayName();
                    String uid = acct.getId();
                    String photoUrl = acct.getPhotoUrl() != null ? acct.getPhotoUrl().toString() : null;
                    showDialogue(displayName, email, uid, photoUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", databaseError.getMessage());
                Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendUserDataToBackend(String displayName, String email, String uid, String pass, Bitmap profileImageBitmap) {
        SignupRequest signupRequest = new SignupRequest(displayName, displayName, email, pass, "", 0.0F, false, false);

        signupRequest.setUserName(displayName);
        signupRequest.setFullName(displayName);
        signupRequest.setEmail(email);
        signupRequest.setPassword(pass);
        signupRequest.setWaterLevel(0.0F);
        signupRequest.setSafetyAlert(false);
        signupRequest.setQualityAlert(false);

        if (profileImageBitmap != null) {
            String profileImageBase64 = encodeImageToBase64(profileImageBitmap);
            signupRequest.setImage(profileImageBase64); // Make sure the field name matches
        } else {
            signupRequest.setImage(null); // Set to null if not provided
        }
        loadingDialog.show();
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<SignupResponse> call = apiService.signup(signupRequest);
        call.enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    SignupResponse signupResponse = response.body();
                    /*Toast.makeText(LoginActivity.this, signupResponse.getMessage(), Toast.LENGTH_SHORT).show();*/
                    createFirebaseUser(email, pass,signupRequest);

                } else {
                    int statusCode = response.code();
                    Log.e("SignupError", "Request failed with status code: " + statusCode);
                    if (response.errorBody() != null) {
                        try {
                            String errorResponse = response.errorBody().string();
                            Log.e("SignupError", "Error body: " + errorResponse);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Signup failed: " + statusCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void createFirebaseUser(String email, String password, SignupRequest signupRequest) {
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
                                            Toast.makeText(LoginActivity.this, "Check your email before login!", Toast.LENGTH_SHORT).show();
                                            /*navigateToMainActivity(signupRequest);*/
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void showDialogue(String displayName, String email, String uid, String photoUrl) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.change_password, null);

        EditText dialogEditText = dialogView.findViewById(R.id.dialog_edittext);
        Button buttonCancel = dialogView.findViewById(R.id.dialog_button_cancel);
        Button buttonOk = dialogView.findViewById(R.id.dialog_button_ok);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                if (photoUrl != null) {
                    Glide.with(LoginActivity.this)
                            .asBitmap()
                            .load(photoUrl)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    // Here you can set the Bitmap to your SignupRequest
                                    sendUserDataToBackend(displayName, email, uid, inputText, resource);
                                }
                            });
                } else {
                    sendUserDataToBackend(displayName, email, uid, inputText, null);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        loadingDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<LoginResponse> call = apiService.login(new LoginRequest(email, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse loginResponse = response.body();
                    Log.d("LoginResponse", "displayName: " + loginResponse.getDisplayName());
                    Log.d("LoginResponse", "email: " + loginResponse.getEmail());
                    Log.d("LoginResponse", "expiresIn: " + loginResponse.getExpiresIn());
                    Log.d("LoginResponse", "idToken: " + loginResponse.getIdToken());
                    Log.d("LoginResponse", "localId: " + loginResponse.getLocalId());
                    Log.d("LoginResponse", "refreshToken: " + loginResponse.getRefreshToken());
                    Log.d("LoginResponse", "registered: " + loginResponse.getRegistered());


                    saveLoginState(loginResponse.getLocalId());

                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Error", t.getMessage());
            }
        });
    }
    private void performLogin(String email,String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<LoginResponse> call = apiService.login(new LoginRequest(email, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse loginResponse = response.body();
                    Log.d("LoginResponse", "displayName: " + loginResponse.getDisplayName());
                    Log.d("LoginResponse", "email: " + loginResponse.getEmail());
                    Log.d("LoginResponse", "expiresIn: " + loginResponse.getExpiresIn());
                    Log.d("LoginResponse", "idToken: " + loginResponse.getIdToken());
                    Log.d("LoginResponse", "localId: " + loginResponse.getLocalId());
                    Log.d("LoginResponse", "refreshToken: " + loginResponse.getRefreshToken());
                    Log.d("LoginResponse", "registered: " + loginResponse.getRegistered());
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    emails=loginResponse.getEmail();
                    saveLoginState(loginResponse.getLocalId());


                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Error", t.getMessage());
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveLoginState(String localId) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("localId", localId);
        editor.putString("email", emails);
        editor.apply();
        navigateToMainActivity();
    }

}
