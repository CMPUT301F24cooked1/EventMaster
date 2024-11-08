package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays the Main Admin panel
 * Used to navigate to all datatypes (events, profiles, images, facilities, qrcode)
 */
public class AdminActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> allEventsResultLauncher;
    private ActivityResultLauncher<Intent> allProfilesResultLauncher;
    private ActivityResultLauncher<Intent> allImagesResultLauncher;
    private ActivityResultLauncher<Intent> allFacilitiesResultLauncher;
    private ActivityResultLauncher<Intent> allQRCodeResultLauncher;

    private ActivityResultLauncher<Intent> settingResultLauncher;
    private ActivityResultLauncher<Intent> profileResultLauncher;

    private Profile user;


    // Buttons corresponding to their next screen
    private View eventsButton;
    private View profilesButton;
    private View imagesButton;
    private View facilitiesButton;
    private View qrCodeButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.admin_screen);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("User")) {
            user = (Profile) intent.getSerializableExtra("User");
        } else {
            Log.d("AdminLoginActivity", "No User data found in Intent");
        }

        launchEvents();
        launchProfiles();
        launchImages();
        launchFacilities();
        launchQRCode();

        goBack();
        goToSettings();
        goToProfile();
    }

    /**
     * brings the user to the settings page
     */
    private void goToSettings(){
        settingResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );
        ImageButton settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, SettingsScreen.class);
                intent.putExtra("User", user);
                settingResultLauncher.launch(intent);
            }
        });
    }

    /**
     * brings the user to the profile page
     */
    private void goToProfile(){
        profileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );
        ImageButton profileButton = findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, ProfileActivity.class);
                intent.putExtra("User", user);
                profileResultLauncher.launch(intent);
            }
        });
    }

    /**
     * When the left arrow button is clicked, brings the user back to the previous page
     */
    private void goBack(){
        // Misc. buttons
        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, SettingsScreen.class);
                intent.putExtra("User", user);
                settingResultLauncher.launch(intent);
            }
        });
    }

    /**
     * Sends the user to the admin qrcode screen when the Data button is clicked
     */
    private void launchQRCode(){
        //Links the Settings Screen to the Admin Login screen
        allQRCodeResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );

        qrCodeButton = findViewById(R.id.data_view);
        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Sends the user to the admin facilities screen when the Facility button is clicked
     */
    private void launchFacilities(){
        //Links the Settings Screen to the Admin Login screen
        allFacilitiesResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );

        facilitiesButton = findViewById(R.id.facilities_view);
        facilitiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AdminFacilitiesActivity.class);
                intent.putExtra("User", user);
                allFacilitiesResultLauncher.launch(intent);
            }
        });
    }

    /**
     * Sends the user to the admin images screen when the Image button is clicked
     */
    private void launchImages(){
        //Links the Settings Screen to the Admin Login screen
        allImagesResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );

        imagesButton = findViewById(R.id.images_view);
        imagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AdminImagesActivity.class);
                intent.putExtra("User", user);
                allImagesResultLauncher.launch(intent);
            }
        });
    }

    /**
     * Sends the user to the admin profiles screen when the Profile button is clicked
     */
    private void launchProfiles(){
        //Links the Settings Screen to the Admin Login screen
        allProfilesResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }
                }
        );

        profilesButton = findViewById(R.id.profiles_view);
        profilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AdminProfileActivity.class);
                intent.putExtra("User", user);
                allProfilesResultLauncher.launch(intent);
            }
        });
    }

    /**
     *  Sends the user to the admin events screen when the Event button is clicked
     */
    private void launchEvents(){
        //Links the Settings Screen to the Admin Login screen
        allEventsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );
        eventsButton = findViewById(R.id.events_view);
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AdminEventActivity.class);
                intent.putExtra("User", user);
                allEventsResultLauncher.launch(intent);
            }
        });
    }
}
