package com.example.eventmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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

public class SettingsScreen extends AppCompatActivity {
    /**
     * Initializes the Setting Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view and select all setting options
     */
    private Switch notificationSwitch;
    private ImageButton backButton;
    private ActivityResultLauncher<Intent> settingResultLauncher;
    private ActivityResultLauncher<Intent> profileResultLauncher;
    private View adminPrivilegesButton;
    private ActivityResultLauncher<Intent> adminCodeResultLauncher;
    private ActivityResultLauncher<Intent> appInfoResultLauncher;
    private View appInfoButton;
    private Switch modeSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(SettingsScreen.this);
        setContentView(R.layout.setting_screen);

        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        notificationSwitch = findViewById(R.id.notification_button);

        // Toggle on and off the notification button
        notificationSwitch.setChecked(true); // enabled notifications
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean toggleNotifications) {
                if (toggleNotifications){
                    user.setNotifications(true);  // set notifications to on
                //    Toast.makeText(SettingsScreen.this, "Notifications ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    user.setNotifications(false);
                //    Toast.makeText(SettingsScreen.this, "Notifications OFF", Toast.LENGTH_SHORT).show();
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

        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to MainActivity class
                Intent intent = new Intent(SettingsScreen.this, MainActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
            }
        });

        ImageButton profileButton = findViewById(R.id.profile);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to ProfileActivity class
                Intent intent = new Intent(SettingsScreen.this, ProfileActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
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



}
