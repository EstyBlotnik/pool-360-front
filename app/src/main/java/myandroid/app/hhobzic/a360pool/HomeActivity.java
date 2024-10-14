package myandroid.app.hhobzic.a360pool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.fragments.AccountFragment;
import myandroid.app.hhobzic.a360pool.fragments.FIxingFragment;
import myandroid.app.hhobzic.a360pool.fragments.HistoryFragment;
import myandroid.app.hhobzic.a360pool.fragments.HomeFragment;
import myandroid.app.hhobzic.a360pool.fragments.MeasuresFragment;
import myandroid.app.hhobzic.a360pool.fragments.PoolSettingsFragment;
import myandroid.app.hhobzic.a360pool.fragments.SupportFragment;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.a360pool.service.FrequentTaskService;
import myandroid.app.hhobzic.a360pool.service.SensorCheckWorker;
import myandroid.app.hhobzic.pool360.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.concurrent.TimeUnit;

import myandroid.app.hhobzic.pool360.databinding.ActivityHomeBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements FIxingFragment.OnFixingFragmentInteractionListener {

    private ActivityHomeBinding binding;
    private TextView headerName,userName;
    private RoundedImageView userImage;
    private LoadingDialog loadingDialog;
    private static final int REQUEST_CALL_PHONE_PERMISSION = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 123;
    float waterLevel;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable periodicTask;
    private boolean isTaskScheduled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this, "Loading...");

        checkPermissions();
        scheduleSensorCheck();
        /*Intent serviceIntent = new Intent(this, FrequentTaskService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(serviceIntent);
        }*/

        binding.menu.setOnClickListener(v -> binding.drawerLayout.open());

        View headerView = binding.navigationView.getHeaderView(0);
        headerName = binding.headerName;
        userImage = headerView.findViewById(R.id.userImage);
        userName = headerView.findViewById(R.id.userName);

        ImageView closeButton = headerView.findViewById(R.id.closeButton);
        TextView logout = headerView.findViewById(R.id.logoutTextView);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearLoginState();

                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        closeButton.setOnClickListener(v -> binding.drawerLayout.close());

        updateBottomNavSelection(R.id.home);

        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.nav_home) {
                    loadFragment(new HomeFragment(), "");
                } else if (itemId == R.id.nav_pool_settings) {
                    loadFragment(new PoolSettingsFragment(), "Pool Settings");
                } else if (itemId == R.id.nav_account) {
                    loadFragment(new AccountFragment(), "My Account");
                }  else if (itemId == R.id.nav_alerts) {
                    loadFragment(new HistoryFragment(), "Alerts");
                } else if (itemId == R.id.nav_support) {
                    loadFragment(new SupportFragment(), "Support");
                } else {
                    return false;
                }
                binding.drawerLayout.close();
                return true;
            }
        });

        binding.home.setOnClickListener(v -> selectBottomNavItem(R.id.home, ""));
        binding.measures.setOnClickListener(v -> selectBottomNavItem(R.id.measures, "Water Measurements"));
        binding.history.setOnClickListener(v -> selectBottomNavItem(R.id.history, "Alerts"));
        binding.support.setOnClickListener(v -> selectBottomNavItem(R.id.support, "Support"));
        binding.sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateSOSCall();
            }
        });

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "Home");
        }
        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Delete History")
                        .setMessage("Do you want to delete all history?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                                DatabaseReference sensorDataRef = databaseReference.child("issues");

                                sensorDataRef.removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "History deleted Successfully!", Toast.LENGTH_SHORT).show();
                                                    Fragment historyFragment = getSupportFragmentManager().findFragmentByTag("Alerts");
                                                    if (historyFragment instanceof HistoryFragment) {
                                                        ((HistoryFragment) historyFragment).refreshData();
                                                    }
                                                    loadUserData();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Failed to delete History!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User chose "No", so do nothing
                                dialog.dismiss();
                            }
                        });

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        loadUserData();

    }

    @SuppressLint("ObsoleteSdkInt")
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Other permission checks if needed
        }
    }
    private void initiateSOSCall() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + "101"));
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE_PERMISSION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void getNotificationPermission(){
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }catch (Exception e){

        }
    }
    private void clearLoginState() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void selectBottomNavItem(int itemId, String fragmentName) {
        if (itemId == R.id.home) {
            loadFragment(new HomeFragment(), fragmentName);
            updateBottomNavSelection(R.id.home);
        } else if (itemId == R.id.measures) {
            loadFragment(new MeasuresFragment(), fragmentName);
            updateBottomNavSelection(R.id.measures);
        }else if (itemId == R.id.history) {
            loadFragment(new HistoryFragment(), fragmentName);
            updateBottomNavSelection(R.id.history);
        } else if (itemId == R.id.support) {
            loadFragment(new SupportFragment(), fragmentName);
            updateBottomNavSelection(R.id.support);
        }
    }

    private void updateBottomNavSelection(int selectedId) {
        int colorGray = ContextCompat.getColor(this, R.color.appGray);
        int colorSelected = ContextCompat.getColor(this, R.color.black);

        ((ImageView) binding.home.getChildAt(0)).setColorFilter(colorGray);
        ((ImageView) binding.measures.getChildAt(0)).setColorFilter(colorGray);
        ((ImageView) binding.history.getChildAt(0)).setColorFilter(colorGray);
        ((ImageView) binding.support.getChildAt(0)).setColorFilter(colorGray);

        if (selectedId == R.id.home) {
            ((ImageView) binding.home.getChildAt(0)).setColorFilter(colorSelected);
        } else if (selectedId == R.id.measures) {
            ((ImageView) binding.measures.getChildAt(0)).setColorFilter(colorSelected);
        }else if (selectedId == R.id.history) {
            ((ImageView) binding.history.getChildAt(0)).setColorFilter(colorSelected);
        } else if (selectedId == R.id.support) {
            ((ImageView) binding.support.getChildAt(0)).setColorFilter(colorSelected);
        }
    }

    private void loadFragment(Fragment fragment, String fragmentName) {
        if (fragmentName.equals("Alerts")) {
            binding.userImage.setVisibility(View.GONE);
            binding.delete.setVisibility(View.VISIBLE);
        } else {
            binding.userImage.setVisibility(View.VISIBLE);
            binding.delete.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
        headerName.setText(fragmentName);
    }

    private void loadUserData() {
        loadingDialog.show();
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
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
                        userName.setText(userData.getUserName());
                        String profileImageBase64 = userData.getProfileImage();

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty())
                        {
                            Bitmap decodedByte = base64ToBitmap(profileImageBase64);
                            userImage.setImageBitmap(decodedByte);
                            binding.userImage.setImageBitmap(decodedByte);
                            waterLevel=userData.getWaterLevel();
                        }
                        else {
                            Toast.makeText(HomeActivity.this, "No Image Found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GetUsersData> call, Throwable t) {
                    loadingDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadingDialog.dismiss();
            Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CALL_PHONE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, initiate call
                    initiateSOSCall();
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(this, "Permission to make phone calls is required to use SOS", Toast.LENGTH_SHORT).show();
                }
            case REQUEST_NOTIFICATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted for notifications
                } else {

                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isTaskScheduled) {
            handler.post(periodicTask);
            isTaskScheduled = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(periodicTask);
        isTaskScheduled = false;
    }

    public void scheduleSensorCheck() {
        WorkRequest sensorCheckRequest = new PeriodicWorkRequest.Builder(SensorCheckWorker.class, 2, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(sensorCheckRequest);
    }

    @Override
    public void onFixingFragmentClosed() {
        FrameLayout frameLayout = findViewById(R.id.fragment_container);
        if (frameLayout != null) {
            frameLayout.setVisibility(View.GONE);
        }
    }
}
