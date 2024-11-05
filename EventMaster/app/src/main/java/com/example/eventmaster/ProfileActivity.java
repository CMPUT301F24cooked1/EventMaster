package com.example.eventmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

/**
 * The class represents the profile screen where the user's information is displayed.
 * It initializes the profile screen, displays uer information like nane, email
 * and phone number. It also handles navigation between profile and other activities.
 *
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView displayName;
    private TextView displayEmail;
    private TextView displayPhoneNumber;
    private AppCompatButton editProfileButton;
    private ImageButton backButton;
    private ActivityResultLauncher<Intent> editProfileResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private ImageButton settingsButton;
    private ActivityResultLauncher<Intent> settingResultLauncher;

    /**
     * Initializes the profile screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * Sets the user's inputted information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // User from MainActivity
        ImageView profile_picture = findViewById(R.id.profile_picture );

        Image.getProfilePictureUrl(user.getDeviceId(), user, profile_picture, ProfileActivity.this);

        displayName = findViewById(R.id.profile_name);
        displayEmail = findViewById(R.id.profile_email);
        displayPhoneNumber = findViewById(R.id.profile_phone_number);
        editProfileButton = findViewById(R.id.edit_profile_button);
        backButton = findViewById(R.id.back);

        displayName.setText(user.getName());
        displayEmail.setText(user.getEmail());
        displayPhoneNumber.setText(user.getPhone_number());

        editProfileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        settingResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, InputUserInformation.class);
                intent.putExtra("User", user);
                editProfileResultLauncher.launch(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("User", user);
                MainActivityResultLauncher.launch(intent);
            }
        });

        settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingsScreen.class);
                intent.putExtra("User", user);
                settingResultLauncher.launch(intent);
            }
        });

    }






}
