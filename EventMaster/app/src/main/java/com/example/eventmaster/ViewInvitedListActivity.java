package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for viewing a sampled list of entrants who have been invited to an event.
 */
public class ViewInvitedListActivity extends AppCompatActivity {

    private RecyclerView invitedRecyclerView;
    private WaitlistUsersAdapter invitedAdapter;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private String eventName; // Define event name or ID
    private List<WaitlistUsersAdapter.User> invitedUsers;
    private List<String> invitedIds;
    private Profile user;
    private int selected;

    private AppCompatButton notifyButton;
    private AppCompatButton rejectedListButton;

    /**
     * Displays the list of users who have been invited to come to an Event.
     * @param savedInstanceState If the activity is restarted this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_invited_list);

        Intent intentMain = getIntent();
        eventName = intentMain.getStringExtra("myEventName");
        user = (Profile) intentMain.getSerializableExtra("User");
        selected = intentMain.getIntExtra("Sampled?", 0);

        invitedUsers = new ArrayList<>();
        invitedIds = new ArrayList<>();

        invitedRecyclerView = findViewById(R.id.waitlistRecyclerView);
        invitedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass the context and eventName to the adapter
        invitedAdapter = new WaitlistUsersAdapter(invitedUsers, this, eventName);
        invitedRecyclerView.setAdapter(invitedAdapter);

        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Fetch the waitlist from Firebase
        fetchInvitedList();

        if (selected == 1) {
            Toast.makeText(ViewInvitedListActivity.this, "Copying unsampled list", Toast.LENGTH_SHORT).show();
            copyUnsampledList();
        }

        notifyButton = findViewById(R.id.send_notification_button);
        rejectedListButton = findViewById(R.id.rejected_list_button);

        // Initialize navigation buttons
        ImageButton notificationButton = findViewById(R.id.notifications);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton homeButton = findViewById(R.id.home_icon);
        ImageButton backButton = findViewById(R.id.back_button); // Initialize back button
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        notifyButton.setOnClickListener(v -> {
            String notifyDate = String.valueOf(System.currentTimeMillis());
            setNotifiedInFirestore(eventName, notifyDate);
        });

        // Set click listeners for navigation
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewInvitedListActivity.this, Notifications.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewInvitedListActivity.this, SettingsScreen.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewInvitedListActivity.this, ProfileActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewInvitedListActivity.this, MainActivity.class);
            startActivity(intent);
        });
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            finish(); // Close the current activity and return to the previous one
        });
    }

    /**
     * Gets the invited list from a specific event and calls fetchUserName on each device ID.
     */
    private void fetchInvitedList() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("invited list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userDeviceId = document.getId();
                            invitedIds.add(userDeviceId);
                            fetchUserName(userDeviceId); // Fetch the name for each userDeviceId
                        }
                    } else {
                        Log.e("WaitlistActivity", "Error getting waitlist: ", task.getException());
                    }
                });
    }

    //
    /**
     * Fetches the user's name and profile picture URL based on the device ID. Saves them to invitedUsers
     * @param userDeviceId The device ID to fetch the username and profile picture from.
     */
    private void fetchUserName(String userDeviceId) {
        firestore.collection("profiles")
                .document(userDeviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name"); // Assuming 'name' is the field for the user's name
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl"); // Assuming 'profilePictureUrl' is the field for profile picture

                        if (userName != null) {
                            // Create a User object and add it to the waitlist
                            WaitlistUsersAdapter.User user = new WaitlistUsersAdapter.User(userName, profilePictureUrl);
                            invitedUsers.add(user);
                            invitedAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitlistActivity", "Error fetching user data", e));
    }

    private void setNotifiedInFirestore(String eventName, String notifyDate) {
        for (int i = 0; i < invitedIds.size(); i++) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notifyDate", notifyDate);

            String entrantId = invitedIds.get(i);
            firestore.collection("entrants")
                    .document(entrantId)
                    .collection("Invited Events")
                    .document(eventName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firestoreNotifyDate = documentSnapshot.getString("notifyDate");
                            if (firestoreNotifyDate == null) {
                                firestore.collection("entrants")
                                        .document(entrantId)
                                        .collection("Invited Events")
                                        .document(eventName)
                                        .set(notificationData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Notify Users", "User set as notified in firestore");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Notify Users", "Error setting user as notified in firestore", e);
                                        });
                            } else {
                                Log.d("Notify Users", "User has already been notified");
                            }
                        }
                    });
        }
        Toast.makeText(ViewInvitedListActivity.this, "Previously un-notified users have been notified.", Toast.LENGTH_SHORT).show();
    }

    private void copyUnsampledList() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("unsampled list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userDeviceId = document.getId(); // Fetch the name for each userDeviceId
                            copyUnsampledUser(userDeviceId);
                            updateRejectedEvents(userDeviceId, eventName);
                        }
                    } else {
                        Log.e("Rejected List", "Error getting retrieving unsampled list: ", task.getException());
                    }
                });
    }

    private void copyUnsampledUser(String userDeviceId) {

        Map<String, Object> rejectedData = new HashMap<>();
        rejectedData.put("eventId", eventName);

        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("rejected list")
                .document(userDeviceId)
                .set(rejectedData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Device ID field updated in rejected list successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating device ID field in rejected list", e);
                });
    }

    private void updateRejectedEvents(String entrantId, String eventName) {
        Map<String, Object> rejectedEntrantData = new HashMap<>();
        rejectedEntrantData.put("Event Name", eventName);
        rejectedEntrantData.put("facilityId", deviceId);

        //Add Event name to Entrant in Firestore under Invited Events
        firestore.collection("entrants")
                .document(entrantId)
                .collection("Rejected Events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firestore.collection("entrants")
                                .document(entrantId)
                                .collection("Rejected Events")
                                .document(eventName)
                                .set(rejectedEntrantData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Rejected Events", "Device ID field updated in entrant's rejected lists successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Rejected Events", "Error updating device ID field in rejected list", e);
                                });
                    } else {
                        Log.e("Rejected Events", "Failed to add user to rejected events", task.getException());
                    }
                });
    }
}