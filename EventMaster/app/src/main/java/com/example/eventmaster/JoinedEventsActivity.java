package com.example.eventmaster;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the Joined Events Screen
 * Shows a list of the events that an entrant has been accepted to
 */
public class JoinedEventsActivity extends AppCompatActivity {
    /**
     * Initializes the Joined Events Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view all joined events
     */
    private RecyclerView recyclerView;
    // private EventAdapter eventAdapter;
    private ViewJoinedEventsAdapter ViewJoinedEventsAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private String facilityId;
    private String eventName;
    private String eventDescription;

    private FirebaseFirestore db; // Firestore instance
    private ActivityResultLauncher<Intent> waitlistedEventsActivityResultLauncher;
    private Profile user;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;

    private ActivityResultLauncher<Intent> homeActivityResultLauncher;

    //  private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joined_events_screen);
        ModeActivity.applyTheme(this);
        user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity



        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();

        ViewJoinedEventsAdapter = new ViewJoinedEventsAdapter(eventList, this, user);
        recyclerView.setAdapter(ViewJoinedEventsAdapter);

        // Retrieve events from Firestore
        retrieveJoinedEvents(deviceId);

        // Connecting joined events screen to waitlisted events screen
        waitlistedEventsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        //links joined events screen to the waitlisted events screen
        AppCompatButton viewWaitlist = findViewById(R.id.view_waitlist_button);
        viewWaitlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinedEventsActivity.this, WaitlistedEventsActivity.class);
                intent.putExtra("User", user);
                waitlistedEventsActivityResultLauncher.launch(intent);
            }
        });


        // Initialize navigation buttons
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
                newIntent = new Intent(JoinedEventsActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(JoinedEventsActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(JoinedEventsActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(JoinedEventsActivity.this, ProfileActivity.class);
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
     * Retrieves the event details for the joined event
     * @param eventName
     * @param deviceId
     * @param listener
     */

    private void fetchEventDetails(String eventName, String deviceId, OnEventDetailsFetchedListener listener) {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            String eventDescription = doc.getString("eventDescription");
                            listener.onEventDetailsFetched(eventDescription);
                        } else {
                            Log.d("FetchEventDetails", "Document does not exist");
                            listener.onEventDetailsFetched(null);
                        }
                    } else {
                        Log.d("FetchEventDetails", "Task failed: ", task.getException());
                    }
                });

    }

    /**
     * Retrieves all the joined events that the entrant is apart of
     * @param deviceId
     */

    private void retrieveJoinedEvents(String deviceId) {
        //TODO: Retrieve the events that the user has joined from firestore

        DocumentReference entrantDocRef = firestore.collection("entrants").document(deviceId);

        entrantDocRef.collection("Invited Events")
                .whereEqualTo("choiceStatus", "accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document: task.getResult()) {
                            String eventName = document.getString("Event Name");
                            String facilityId = document.getString("facilityId");

                            fetchEventDetails(eventName, facilityId, eventDescription -> {
                                Event event = new Event();
                                event.setDeviceID(deviceId);
                                event.setEventName(eventName);
                                event.setEventDescription(eventDescription);
                                eventList.add(event);

                                if (eventList.size() == task.getResult().size()) {
                                    ViewJoinedEventsAdapter.notifyDataSetChanged();
                                }

                                Log.d("retrieveEventDescription", "Event: " + eventDescription);

                            });
                        }
                        //ViewEventsAdapter.notifyDataSetChanged();
                        Log.d("JoinedEventsScreen", "Number of waitlisted events: " + eventList.size());
                        Log.d("JoinedEventsScreen", "Device ID: " + deviceId);

                    } else {
                        Log.d("JoinedEventsScreen", "QuerySnapshot is null");
                    }
                }).addOnFailureListener(e -> Log.e("JoinedEventsActivity", "Error retrieving joined events", e));
    }

    /**
     * Ensures event description is retrieved synchronously
     */
    interface OnEventDetailsFetchedListener {
        void onEventDetailsFetched(String eventDescription);
    }
}