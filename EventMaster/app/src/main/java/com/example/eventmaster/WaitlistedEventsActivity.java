package com.example.eventmaster;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * The activity represents the screen that displays a list of all the waitlisted events that belong to an entrant
 */
// https://www.c-sharpcorner.com/article/android-qr-code-bar-code-scanner/
public class WaitlistedEventsActivity extends AppCompatActivity {
    /**
     * Initializes the Join Event Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view all events and select any event
     */
    private RecyclerView recyclerView;
    private WaitlistedEventsAdapter WaitlistedEventsAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String entrantId; // Replace with actual device ID
    private FirebaseFirestore db; // Firestore instance
    private Profile user;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;


    //  private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.waitlisted_events_screen);
        user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity



        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        entrantId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();

        WaitlistedEventsAdapter = new WaitlistedEventsAdapter(eventList, this, user);
        WaitlistedEventsAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(WaitlistedEventsAdapter);

        // Retrieve events from Firestore
        retrieveWaitlistedEvents(entrantId);


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
                newIntent = new Intent(WaitlistedEventsActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(WaitlistedEventsActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(WaitlistedEventsActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(WaitlistedEventsActivity.this, ProfileActivity.class);
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
     * Retrieve the waitlisted events for specific entrant
     * @param entrantId
     */
    private void retrieveWaitlistedEvents(String entrantId) {
        //eventList.clear();
        DocumentReference entrantDocRef = firestore.collection("entrants").document(entrantId);

        // Retrieve waitlisted events for the specific entrant
        entrantDocRef.collection("Waitlisted Events").get().addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                    Event event = eventDoc.toObject(Event.class);
                    event.setDeviceID(entrantId);  // Set the device ID as the entrant ID
                    eventList.add(event);
                }
                WaitlistedEventsAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                Log.d("JoinEventScreen", "Number of waitlisted events: " + eventList.size());
                Log.d("JoinEventScreen", "Device ID: " + entrantId);

            } else {
                Log.d("WaitlistedEventsActivity", "QuerySnapshot is null");
            }
        }).addOnFailureListener(e -> Log.e("WaitlistedEventsActivity", "Error retrieving waitlisted events", e));
    }



}


