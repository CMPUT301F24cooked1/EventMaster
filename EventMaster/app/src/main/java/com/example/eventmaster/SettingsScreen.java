package com.example.eventmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays the settings screen
 * Links the setting screen to the Admin Login screen and the App Info screen
 */
public class SettingsScreen extends AppCompatActivity {

    private Switch notificationSwitch;
    private ImageButton backButton;
    private Profile user;
    private ActivityResultLauncher<Intent> settingResultLauncher;
    private ActivityResultLauncher<Intent> profileResultLauncher;
    private View adminPrivilegesButton;
    private ActivityResultLauncher<Intent> adminCodeResultLauncher;
    private ActivityResultLauncher<Intent> appInfoResultLauncher;
    private View appInfoButton;
    private Switch modeSwitch;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;

    private ActivityResultLauncher<Intent> listResultLauncher;
    private FirebaseFirestore db;

    /**
     * Initializes the Setting Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view and select all setting options
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(SettingsScreen.this);
        setContentView(R.layout.setting_screen);
        db = FirebaseFirestore.getInstance();

        user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity


        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        notificationSwitch = findViewById(R.id.notification_button);


        notificationSetting(deviceId, "entrants");  // make sure toggle button doesn't reset

        // Toggle on and off the notification button
        notificationSwitch.setChecked(true); // enabled notifications as default

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean toggleNotifications) {
                String notificationStatus = toggleNotifications ? "on" : "off";
                storeNotificationSetting(deviceId, "entrants", notificationStatus);
                if (toggleNotifications){
                    user.setNotifications(false);  // set notifications to on
                    Toast.makeText(SettingsScreen.this, "Notifications On", Toast.LENGTH_SHORT).show();
                }
                else{  // set notifications to off
                    user.setNotifications(true);
                    Toast.makeText(SettingsScreen.this, "Notifications Off", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // Toggles on and off dark mode
        SharedPreferences sharedPreferences = getSharedPreferences("themePrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);

        modeSwitch = findViewById(R.id.mode_switch);
        modeSwitch.setChecked(isDarkMode);

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("SettingsScreen", "Switch toggled. Current state: " + isChecked);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("darkMode", isChecked);
                editor.apply();

                Log.d("SettingsScreen", "Applying mode change: " + (isChecked ? "Dark Mode" : "Light Mode"));

                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
                recreate();
            }
        });


        //Links the Settings Screen to the Admin Login screen
        adminCodeResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );
        adminPrivilegesButton = findViewById(R.id.admin_privileges_view);
        adminPrivilegesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to SettingsScreen class
                Log.d("SettingsScreen", "Admin privileges button clicked");

                Intent intent = new Intent(SettingsScreen.this, AdminLoginActivity.class);
                intent.putExtra("User", user);
                adminCodeResultLauncher.launch(intent);
            }
        });

        appInfoResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );

        appInfoButton = findViewById(R.id.app_info);
        appInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to appInfo screen
                Intent intent = new Intent(SettingsScreen.this, AppInfoActivity.class);
                intent.putExtra("User", user);
                appInfoResultLauncher.launch(intent);
            }
        });

        // Initialize navigation buttons
        // Set result launchers to set up navigation buttons on the bottom of the screen
        settingsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        notificationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user=updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        ProfileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user=updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });


        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Disable tint for specific menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem qrCodeItem = menu.findItem(R.id.nav_scan_qr);
        Drawable qrIcon = qrCodeItem.getIcon();
        qrIcon.setTintList(null);  // Disable tinting for this specific item
        // Set up navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent newIntent;

            if (item.getItemId() == R.id.nav_Home) {
                newIntent = new Intent(SettingsScreen.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(SettingsScreen. this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(SettingsScreen.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(SettingsScreen.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_scan_qr) {
                // Open QRScanFragment without simulating button click
                openQRScanFragment();
                return true;
            }
            return false;
        });

    }
    private void openQRScanFragment() {
        // Open QRScanFragment without simulating button click
        Intent intent = new Intent(this, QRScanFragment.class);
        intent.putExtra("User", user);  // Pass the user information if needed
        startActivity(intent);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Log only for debug purposes, do not reapply theme logic here
        if ((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            Log.d("SettingsScreen", "Night mode activated.");
        } else if ((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
            Log.d("SettingsScreen", "Day mode activated.");
        }
    }

    /**
     * Defaults notifications to on and saves the toggle position when re running the project
     * @param deviceId the deviceID of the user's device
     * @param path the collection under which to store the deviceId
     */
    private void notificationSetting(String deviceId, String path) {
        db.collection(path).document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String notifications = document.getString("notifications");
                            if (notifications != null) {
                                notificationSwitch.setChecked("on".equals(notifications));  // set where the button is toggeled on or off
                            }
                        } else {
                            notificationSwitch.setChecked(true);  // default to notificataions on
                        }
                    }
                });
    }

    /**
     * Adds notifications status to firestore DB in a given collection, checks if notifications status has already been added.
     * If already in DB, does not add notifications status
     * If NOT in DB, adds notifications status to DB
     * @param deviceId the deviceID of the user's device
     * @param path the collection under which to store the deviceId
     * @param notification the collection under which to store the notifications status
     */
    private void storeNotificationSetting(String deviceId, String path, String notification) {
        db.collection(path).document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            db.collection(path).document(deviceId)
                                    .update("notifications", notification);  // update notifications in firebase
                        } else {
                            // create notifications if it does not exist
                            Map<String, Object> notificationData = new HashMap<>();
                            notificationData.put("notifications", notification);
                            notificationData.put("deviceId", deviceId);

                            db.collection(path).document(deviceId)
                                    .set(notificationData, SetOptions.merge());
                        }
                    }
                });
    }







}
