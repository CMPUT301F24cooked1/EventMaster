package com.example.eventmaster;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "ProfilePrefs";
    private TextView displayName;
    private TextView displayEmail;
    private TextView displayPhoneNumber;
    private AppCompatButton editProfileButton;
    private ImageButton backButton;
    private ImageButton settingsButton;
    private ImageButton listButton;
    private ImageView profilePicture;
    private Profile user;
    private ActivityResultLauncher<Intent> editProfileResultLauncher;
    private ActivityResultLauncher<Intent> mainActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> listActivityResultLauncher;

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
        backButton = findViewById(R.id.back);
        settingsButton = findViewById(R.id.settings);
        listButton = findViewById(R.id.list_icon);

        displayName = findViewById(R.id.profile_name);
        displayEmail = findViewById(R.id.profile_email);
        displayPhoneNumber = findViewById(R.id.profile_phone_number);
        editProfileButton = findViewById(R.id.edit_profile_button);

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

        mainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );

        settingsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );

        listActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );

        // sends you to profile (ur already in it)
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, InputUserInformation.class);
            intent.putExtra("User", user);
            editProfileResultLauncher.launch(intent);
        });

        // sends you back to previous screen
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.putExtra("User", user);
            mainActivityResultLauncher.launch(intent);
        });

        // sends you to settings screen
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsScreen.class);
            intent.putExtra("User", user);
            settingsResultLauncher.launch(intent);
        });

        // sends you to a list of invited events that you accepted
        listButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, JoinedEventsActivity.class);
            intent.putExtra("User", user);
            listActivityResultLauncher.launch(intent);
        });

    }

    /**
     * Loads the user’s profile information into the display fields.
     */
    private void loadUserInfo() {
        displayName.setText(user.getName());
        displayEmail.setText(user.getEmail());
        displayPhoneNumber.setText(user.getPhone_number());
        ProfilePicture.loadProfilePicture(user, profilePicture, ProfileActivity.this);
    }


}