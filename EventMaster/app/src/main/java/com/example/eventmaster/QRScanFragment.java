package com.example.eventmaster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * The QRScanFragment class handles QR code scanning functionality and related user interactions.
 * <p>
 * This activity allows users to scan a QR code, which retrieves data for a specific event.
 * The class also includes navigation to other screens with the icons located on the bottom of the screen.
 * </p>
 */
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
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private ImageButton backButton;


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

        backButton = findViewById(R.id.back);
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
                newIntent = new Intent(QRScanFragment.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(QRScanFragment. this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(QRScanFragment.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(QRScanFragment.this, ProfileActivity.class);
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

    // ScanCode is referenced by https://www.c-sharpcorner.com/article/android-qr-code-bar-code-scanner/
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


