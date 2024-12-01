package com.example.eventmaster;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The ProfileActivity class represents the user's profile screen in the app.
 * <p>
 * This activity displays the user's profile information, such as name, email, and phone number.
 * It allows navigation to edit profile details, settings, and a list of joined events.
 * </p>
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "ProfilePrefs";
    private TextView displayName;
    private TextView displayEmail;
    private TextView displayPhoneNumber;
    private AppCompatButton editProfileButton;
    private ImageButton backButton;
    private ImageView profilePicture;
    private Profile user;
    private ActivityResultLauncher<Intent> editProfileResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;


    /**
     * Initializes the ProfileActivity layout and sets up the bottom icons for navigation.
     * Loads the user information from the Profile object.
     *
     * @param savedInstanceState If the activity is restarted this Bundle contains the most recent data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);

        user = (Profile) getIntent().getSerializableExtra("User");
        profilePicture = findViewById(R.id.profile_picture);

        displayName = findViewById(R.id.profile_name);
        displayEmail = findViewById(R.id.profile_email);
        displayPhoneNumber = findViewById(R.id.profile_phone_number);
        editProfileButton = findViewById(R.id.edit_profile_button);
        backButton = findViewById(R.id.back);

        loadUserInfo();

        // update all the inputs once user clicks save
        editProfileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        user = (Profile) result.getData().getSerializableExtra("User");
                        loadUserInfo();
                    }
                }
        );

        // sends you to inputuserinformation screen where user edits their details
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, InputUserInformation.class);
            intent.putExtra("User", user);
            editProfileResultLauncher.launch(intent);
        });


        // sends you to profile (ur already in it)
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, InputUserInformation.class);
            intent.putExtra("User", user);
            editProfileResultLauncher.launch(intent);
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

        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
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
                newIntent = new Intent(ProfileActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(ProfileActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(ProfileActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(ProfileActivity.this, ProfileActivity.class);
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

    /**
     * Loads the user’s profile information into the display fields.
     */
    private void loadUserInfo() {
        displayName.setText(user.getName());
        displayEmail.setText(user.getEmail());
        displayPhoneNumber.setText(user.getPhone_number());
        if (user.getName() != null){
            ProfilePicture.loadProfilePicture(user, profilePicture, ProfileActivity.this);
        }
    }


}