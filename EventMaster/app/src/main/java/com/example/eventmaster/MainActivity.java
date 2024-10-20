package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.home_screen);

        db = FirebaseFirestore.getInstance();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Checks if deviceId was grabbed
        Log.d("DeviceID", "Android ID: " + deviceId);

        Profile user = new Profile(deviceId);
        storeDeviceID(deviceId);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connecting the home screen to the SettingsScreen
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );
        ImageButton settingButton = findViewById(R.id.settings);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send user to settingsScreen class
                Intent intent = new Intent(MainActivity.this, SettingsScreen.class);
                intent.putExtra("User",  user);
                activityResultLauncher.launch(intent);
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


}