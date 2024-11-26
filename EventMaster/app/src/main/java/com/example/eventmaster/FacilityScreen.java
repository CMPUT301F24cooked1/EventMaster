package com.example.eventmaster;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays the name, address and description for the user's facility
 * Allows navigation to creation of an event, viewing events, and editing facility
 */
public class FacilityScreen extends AppCompatActivity {

    private String deviceId;
    private ActivityResultLauncher<Intent> editFacilityResultLauncher;
    private FirebaseFirestore db;

    private Profile user;
    private Facility updatedUserFacility;
    private Facility userFacility;

    private TextView facilityNameText;
    private TextView facilityAddressText;
    private TextView facilityDescText;
    private AppCompatButton editFacilityButton;
    private AppCompatButton viewEventsButton;
    private AppCompatButton createEventButton;
    private ImageButton backButton;
    private ImageButton profileButton;
    private ImageButton settingsButton;
    private ImageButton notificationButton;
    private ImageButton listButton;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;

    /**
     * @param savedInstanceState If the activity is restarted this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.facility_screen);

        db = FirebaseFirestore.getInstance();
        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("User");
        deviceId = user.getDeviceId();

        //Checks if user was grabbed
        assert user != null;

        userFacility = (Facility) intentMain.getSerializableExtra("Facility");

        //Checks if facility was grabbed
        assert userFacility != null;

        //Updates facility info in the database
        updateFacilityInfo(userFacility.getDeviceId(), userFacility.getFacilityName(), userFacility.getFacilityAddress(), userFacility.getFacilityDesc());

        //Set text for Facility fields.
        facilityNameText = findViewById(R.id.facilityNameText);
        facilityAddressText = findViewById(R.id.facilityAddressText);
        facilityDescText = findViewById(R.id.facilityDescText);

        facilityNameText.setText(userFacility.getFacilityName());
        facilityAddressText.setText(userFacility.getFacilityAddress());
        facilityDescText.setText(userFacility.getFacilityDesc());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewEventsButton = findViewById(R.id.view_events_button);

        viewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(FacilityScreen.this, ViewCreatedEventsActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });
        ImageButton backButton = findViewById(R.id.back);
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        // Connecting the facility screen to the Edit Facility Screen
        editFacilityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult returnResult) {
                if (returnResult != null && returnResult.getResultCode() == RESULT_OK) {
                    if (returnResult.getData() != null && returnResult.getData().getSerializableExtra("editedUserFacility") != null) {
                        updatedUserFacility = (Facility) returnResult.getData().getSerializableExtra("editedUserFacility");
                        updateFacilityInfo(updatedUserFacility.getDeviceId(), updatedUserFacility.getFacilityName(), updatedUserFacility.getFacilityAddress(), updatedUserFacility.getFacilityDesc());
                        facilityNameText.setText(updatedUserFacility.getFacilityName());
                        facilityAddressText.setText(updatedUserFacility.getFacilityAddress());
                        facilityDescText.setText(updatedUserFacility.getFacilityDesc());
                        Profile updatedUser = (Profile) returnResult.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                        userFacility = updatedUserFacility;
                    }
                }
            }
        });

        //Move to EditFacilityScreen upon button press, send local Facility object
        editFacilityButton = findViewById(R.id.edit_facility_button);
        editFacilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FacilityScreen.this, EditFacilityScreen.class);
                intent.putExtra("FacilityEdit", userFacility);
                intent.putExtra("FacilityEditUser", user);
                editFacilityResultLauncher.launch(intent);
            }
        });

        //Move to Events Page
        createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FacilityScreen.this, CreateEventActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
            }
        });

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
                newIntent = new Intent(FacilityScreen.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(FacilityScreen.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(FacilityScreen.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(FacilityScreen.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            }else if (item.getItemId() == R.id.nav_scan_qr) {
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
     * updates a user's Facility on firestore DB based on given device ID.
     * @param deviceId the deviceID of the user's device
     * @param facilityName a given string to update the facilityName field on firestore DB with.
     * @param facilityAddress a given string to update the facilityAddress field on firestore DB with.
     * @param facilityDesc a given string to update the facilityDesc field on firestore DB with.
     */
    private void updateFacilityInfo(String deviceId, String facilityName, String facilityAddress, String facilityDesc) {
        // Create a map with the additional user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("deviceId", deviceId);
        userData.put("facilityName", facilityName);
        userData.put("facilityAddress", facilityAddress);
        userData.put("facilityDesc", facilityDesc);

        // Update the document with the new user data, merging with existing data
        db.collection("facilities")
                .document(deviceId)
                .set(userData, SetOptions.merge()) // Merge with existing data
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Facility info updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating Facility info", e);
                });
    }
}