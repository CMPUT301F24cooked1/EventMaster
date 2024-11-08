package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        updateFacilityInfo(userFacility.getDeviceID(), userFacility.getFacilityName(), userFacility.getFacilityAddress(), userFacility.getFacilityDesc());

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

        notificationButton = findViewById(R.id.notification_icon);

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to ProfileActivity class
                Intent intent = new Intent(FacilityScreen.this, Notifications.class);
                intent.putExtra("User", user);
                notificationActivityResultLauncher.launch(intent);
            }
        });

        settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FacilityScreen.this, SettingsScreen.class);
                intent.putExtra("User", user);
                settingsResultLauncher.launch(intent);
            }
        });

        profileButton = findViewById(R.id.profile);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to ProfileActivity class
                Intent intent = new Intent(FacilityScreen.this, ProfileActivity.class);
                intent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(intent);
            }
        });

        listButton = findViewById(R.id.list_icon);

        listButton.setOnClickListener(v -> {
            Intent intent = new Intent(FacilityScreen.this, ViewCreatedEventsActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });

        viewEventsButton = findViewById(R.id.view_events_button);

        viewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(FacilityScreen.this, ViewCreatedEventsActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });

        // Connecting the facility screen to the Edit Facility Screen
        editFacilityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult returnResult) {
                if (returnResult != null && returnResult.getResultCode() == RESULT_OK) {
                    if (returnResult.getData() != null && returnResult.getData().getSerializableExtra("editedUserFacility") != null) {
                        updatedUserFacility = (Facility) returnResult.getData().getSerializableExtra("editedUserFacility");
                        updateFacilityInfo(updatedUserFacility.getDeviceID(), updatedUserFacility.getFacilityName(), updatedUserFacility.getFacilityAddress(), updatedUserFacility.getFacilityDesc());
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


        //Return to home screen, send up to date Facility object
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Returns to previous screen
                Intent newIntent = new Intent(FacilityScreen.this, MainActivity.class);
                newIntent.putExtra("updatedUserFacility", userFacility);
                newIntent.putExtra("User", user);
                setResult(RESULT_OK, newIntent);
                finish();
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