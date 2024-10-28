package com.example.eventmaster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.example.eventmaster.Profile;


import java.security.cert.PKIXRevocationChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;
    private ActivityResultLauncher<Intent> settingResultLauncher;
    private ActivityResultLauncher<Intent> profileResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.home_screen);

        db = FirebaseFirestore.getInstance();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Checks if deviceId was grabbed
        Log.d("DeviceID", "Android ID: " + deviceId);

        Profile user = new Profile(deviceId, " ", " ", Optional.ofNullable(" "));
        storeDeviceID(deviceId);
        updateUserInfo(deviceId, user.getName());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connecting the home screen to the SettingsScreen
        settingResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        // Connecting the home screen to the profile
        profileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        // Reference to the settings button
        ImageButton settingButton = findViewById(R.id.settings);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to SettingsScreen class
                Intent intent = new Intent(MainActivity.this, SettingsScreen.class);
                intent.putExtra("User", user);
                settingResultLauncher.launch(intent);
            }
        });


        ImageButton profileButton = findViewById(R.id.profile);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to ProfileActivity class
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("User", user);
                profileResultLauncher.launch(intent);
            }
        });



    }

    /**
     * Adds deviceID to firestore DB, checks if deviceID has already been added.
     * If already in DB, does not add deviceID
     * If NOT in DB, adds deviceID to DB
     * @param deviceId the deviceID of the user's device
     */
    private void storeDeviceID(String deviceId) {
        db.collection("profiles").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // checks if deviceID is already in Firestore
                        if (task.getResult().exists()) {
                            // deviceID already exists, so do not add the device ID again
                            Log.d("Firestore", "Device ID already exists, skipping insertion.");
                        } else {
                            // deviceID doesn't exist, add the device ID to Firestore
                            Map<String, Object> deviceData = new HashMap<>();
                            deviceData.put("deviceId", deviceId);

                            db.collection("profiles").document(deviceId).set(deviceData) // document is deviceID
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Device ID stored successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error storing device ID", e);
                                    });
                        }
                    } else {
                        Log.e("Firestore", "Error checking if device ID exists", task.getException());
                    }
                });

    }

    private void updateUserInfo(String deviceId, String name) {
        // Create a map with the additional user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);

        // Update the document with the new user data, merging with existing data
        db.collection("profiles")
                .document(deviceId)
                .set(userData, SetOptions.merge()) // Merge with existing data
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User info updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating user info", e);
                });
    }



}