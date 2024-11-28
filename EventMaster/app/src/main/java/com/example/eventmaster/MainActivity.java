package com.example.eventmaster;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.example.eventmaster.Profile;
import com.google.firebase.messaging.FirebaseMessaging;


import java.security.cert.PKIXRevocationChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MainActivity acts as the home screen for the app, and is the starting point
 * where all other navigation follows from.
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;
    private Facility userFacility;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private ActivityResultLauncher<Intent> createEventResultLauncher;
    private ActivityResultLauncher<Intent> joinEventScreenResultLauncher;
    private ActivityResultLauncher<Intent> joinedEventsActivityResultLauncher;

    private String token;
    Profile user;

    //private ActivityResultLauncher<Intent> scanQRFragmentResultLauncher;

    //Request notification permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                } else {

                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Initializes the MainActivity and sets up the icons for navigation.
     * Also checks for device ID and retrieves or stores user data in Firestore.
     *
     * @param savedInstanceState If the activity is restarted this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ModeActivity.applyTheme(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.home_screen);

        db = FirebaseFirestore.getInstance();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Checks if deviceId was grabbed
        Log.d("DeviceID", "Android ID: " + deviceId);

        user = new Profile(deviceId, "", "", ""); // create a new user

        askNotificationPermission(); //Ask for notification permission if not granted.

        storeDeviceID(deviceId, "profiles");
        storeDeviceID(deviceId, "facilities");
        storeDeviceID(deviceId, "entrants");
        storeDeviceID(deviceId, "organizers");
        initializeUser(deviceId); // grab all the user's info from firestore based on device id

        //Get user's notification token for FCM service and upload to firestore
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }

                token = task.getResult();
                uploadToken(token);
                Log.d("Notification Token", "User notification token: " + token);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Connecting home screen to list of joined events screen
        joinedEventsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }
                }
        );

        // Connecting the home screen to the join event screen
        joinEventScreenResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),result ->{
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }
                }
        );



        // Connecting the home screen to the FacilityScreen
        createEventResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult returnResult) {
                if (returnResult != null && returnResult.getResultCode() == RESULT_OK) {
                    if (returnResult.getData() != null && returnResult.getData().getSerializableExtra("updatedUserFacility") != null) {

                        userFacility = (Facility) returnResult.getData().getSerializableExtra("updatedUserFacility");
                        Profile updatedUser = (Profile) returnResult.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }

                    }
                }
            }
        });




        // Reference to the join events button
        Button joinEventButton = findViewById(R.id.join_event_button);

        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to JoinEvent class
                if (user.getName().isEmpty() || user.getEmail().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please set up profile before joining event", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, JoinEventScreen.class);
                intent.putExtra("User", user);
                joinEventScreenResultLauncher.launch(intent);
            }
        });

        userFacility = new Facility(deviceId, "", "", "");

        //Move to Facility Screen, send up to date Facility object
        AppCompatButton createEventButton = findViewById(R.id.create_event_button);

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Find saved deviceId under facilities collection
                DocumentReference docRef = db.collection("facilities").document(deviceId);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //Checks if facilityName/Address/Desc exists yet in database
                                //Updates local values if they do exist

                                Log.d("Facility Data", "DocumentSnapshot data: " + document.getData());

                                Map<String, Object> data = document.getData();

                                if (data.get("facilityName") != null) {
                                    userFacility.setFacilityName(data.get("facilityName").toString());
                                }
                                if (data.get("facilityAddress") != null) {
                                    userFacility.setFacilityAddress(data.get("facilityAddress").toString());
                                }
                                if (data.get("facilityDesc") != null) {
                                    userFacility.setFacilityDesc(data.get("facilityDesc").toString());
                                }

                                Intent intent = new Intent(MainActivity.this, FacilityScreen.class);
                                intent.putExtra("User", user);
                                intent.putExtra("Facility", userFacility);
                                createEventResultLauncher.launch(intent);

                            } else {
                                Log.d("Facility Data", "No such document");
                            }
                        } else {
                            Log.d("Facility Data", "get failed with ", task.getException());
                        }

                    }
                });
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
        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_menu_main);
        // Disable tint for specific menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem qrCodeItem = menu.findItem(R.id.nav_scan_qr);
        Drawable qrIcon = qrCodeItem.getIcon();
        qrIcon.setTintList(null);  // Disable tinting for this specific item
        // Set up navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent newIntent;

            if (item.getItemId() == R.id.nav_Events) {
                newIntent = new Intent(MainActivity.this, JoinedEventsActivity.class);
                newIntent.putExtra("User", user);
                joinedEventsActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(MainActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(MainActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(MainActivity.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            }else if (item.getItemId() == R.id.nav_scan_qr) {
                Intent intent = new Intent(MainActivity.this, QRScannerActivity.class);
                startActivity(intent);
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
     * Adds deviceID to firestore DB in a given collection, checks if deviceID has already been added.
     * If already in DB, does not add deviceID
     * If NOT in DB, adds deviceID to DB
     * @param deviceId the deviceID of the user's device
     * @param path the collection under which to store the deviceId
     */
    private void storeDeviceID(String deviceId, String path) {
        db.collection(path).document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // checks for deviceID in firestore
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Check for "deviceID" field and check if it matches the document ID (which is the deviceID)
                            String storedDeviceId = document.getString("deviceId");
                            if (storedDeviceId == null || !storedDeviceId.equals(deviceId)) {
                                // Update the "deviceId" field to ensure it matches the document ID
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("deviceId", deviceId);

                                db.collection(path).document(deviceId).set(updateData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Device ID field updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error updating device ID field", e);
                                        });
                            } else {
                                Log.d("Firestore", "Device ID field already matches.");
                            }
                        } else {
                            // deviceID doesn't exist, create a new document
                            clearSharedPreferences();
                            Map<String, Object> deviceData = new HashMap<>();
                            deviceData.put("deviceId", deviceId);

                            db.collection(path).document(deviceId).set(deviceData)
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


    /**
     * Initialize user profile with data from firestore
     * @param deviceId the deviceID of the user's device
     */
    public void initializeUser(String deviceId) {
        DocumentReference docRef = db.collection("profiles").document(deviceId);

        // Grabs data from the profile collection
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot != null && snapshot.exists()) {
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    String phone = snapshot.getString("phone number");

                    // Update the user profile object unless it's null then update with empty string
                    user.setDeviceId(deviceId);
                    user.setName(name != null ? name : "");
                    user.setEmail(email != null ? email : "");
                    user.setPhone_number(phone != null ? phone : "");

                    Log.d("Firestore", "User profile initialized: " + user.getName());
                } else {
                    Log.d("Firestore", "Document does not exist");
                }
            } else {
                Log.e("Firestore", "Error fetching document", task.getException());
            }
        });
    }

    private void uploadToken(String notificationToken) {
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("notificationToken", notificationToken);

        db.collection("profiles")
                .document(deviceId)
                .set(tokenData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Notification Token", "Notification token for user uploaded firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification Token", "Error uploading notification token for user in firestore", e);
                });
    }

    /**
     * Removes the User's shared preferences if they don't have a deviceID already in firebase
     * This is used when we delete users off firebase to test the App
     */
    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }




}