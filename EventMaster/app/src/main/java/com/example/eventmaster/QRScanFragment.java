package com.example.eventmaster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;



public class QRScanFragment extends AppCompatActivity{
    /**
     * Initializes the QR scanning
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * Scans a Qr code and sends data to retrieveEventInfo
     */
    private FirebaseFirestore db;
    private Profile user;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> listActivityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_qr_screen);
        user = (Profile) getIntent().getSerializableExtra("User");  // the user information is passed in
        AppCompatButton scanButton = findViewById(R.id.scan_qr_code_button);
        Button button = findViewById(R.id.next_button);


        // click on the scan button to be sent to a QR Scanner
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanCode();
            }
        });

        // Button to skip QR scanning WILL DELETE THIS LATER!!!
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String event = intent.getStringExtra("event");
                String deviceID = intent.getStringExtra("deviceID");

                fetchEventData(deviceID, event);
            }
        });


        // Initialize navigation buttons on the bottom of the screen
        ImageButton notificationButton = findViewById(R.id.notification_icon);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton listButton = findViewById(R.id.list_icon);
        ImageButton backButton = findViewById(R.id.back_button); // Initialize back button

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

        listActivityResultLauncher = registerForActivityResult(
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
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
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
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }


                });

        // Set click listeners for navigation buttons on the bottom of the screen
        // sends you to profile screen
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(QRScanFragment.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        // sends you to settings screen
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(QRScanFragment.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        // sends you to settings screen
        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(QRScanFragment.this, SettingsScreen.class);
            newIntent.putExtra("User", user);
            settingsResultLauncher.launch(newIntent);
        });

        // sends you to a list of invited events that you accepted
        listButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(QRScanFragment.this, MainActivity.class);
            newIntent.putExtra("User", user);
            listActivityResultLauncher.launch(newIntent);
        });
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(QRScanFragment.this, Notifications.class);
            newIntent.putExtra("User", user);
            notificationActivityResultLauncher.launch(newIntent);
        });

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

    }

    // ScanCode is refrenced by https://www.c-sharpcorner.com/article/android-qr-code-bar-code-scanner/
    private void ScanCode() {
        // Set the scanner
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        options.setCaptureActivity(QrCaptureActivity.class);  // access the QrCaptureActivity class to ensure the orientation is vertical
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null)
        {
            // information sent from ViewEventsAdapter class
            String scannedHash = result.getContents();
            Intent intent = getIntent();
            String event = intent.getStringExtra("event");
            String deviceID = intent.getStringExtra("deviceID");  // facility device id

            // create new intent to send information over to the retrieveEventInfo class
            Intent intent2 = new Intent(QRScanFragment.this, retrieveEventInfo.class);
            intent2.putExtra("hashed_data", scannedHash);
            intent2.putExtra("event", event);
            intent2.putExtra("deviceID", deviceID);  // facility device id
            intent2.putExtra("User", user);
            startActivity(intent2);
        }
    });


    // WILL DELETE LATER its for a button to go straight to the next screen without scanning
    private void fetchEventData(String deviceID, String event) {
        db.collection("facilities")
                .document(deviceID)
                .collection("My Events")
                .whereEqualTo("eventName", event) // Adjust the query as necessary
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String hashedData = document.getString("hash");
                                String eventDescription = document.getString("eventDescription");
                                String eventPosterUrl = document.getString("posterUrl");

                                Intent intent2 = new Intent(QRScanFragment.this, retrieveEventInfo.class);
                                intent2.putExtra("hashed_data", hashedData);
                                intent2.putExtra("event", event);
                                intent2.putExtra("deviceID", deviceID);
                                intent2.putExtra("eventDescription", eventDescription);
                                intent2.putExtra("posterUrl", eventPosterUrl);
                                intent2.putExtra("User", user);

                                startActivity(intent2);

                            }
                        } else {
                            Toast.makeText(QRScanFragment.this, "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(QRScanFragment.this, "Error getting data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




}


