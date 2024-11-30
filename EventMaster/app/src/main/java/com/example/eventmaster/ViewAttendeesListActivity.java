package com.example.eventmaster;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Activity for viewing a sampled list of entrants who have been accepted to an event.
 */
public class ViewAttendeesListActivity extends AppCompatActivity {

    private RecyclerView attendeesRecyclerView;
    private WaitlistUsersAdapter attendeesAdapter;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private String eventName; // Define event name or ID
    private List<WaitlistUsersAdapter.User> attendeesUsers;
    private List<String> attendeesIds;
    private Profile user;
    private int selected;
    private String privateKey;

    private AppCompatButton notifyButton;

    /**
     * Displays the list of users who have been accepted to come to an Event.
     * @param savedInstanceState If the activity is restarted this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_attendees_list);

        Intent intentMain = getIntent();
        eventName = intentMain.getStringExtra("myEventName");
        user = (Profile) intentMain.getSerializableExtra("User");
        selected = intentMain.getIntExtra("Sampled?", 0);

        attendeesUsers = new ArrayList<>();
        attendeesIds = new ArrayList<>();

        attendeesRecyclerView = findViewById(R.id.waitlistRecyclerView);
        attendeesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass the context and eventName to the adapter
        attendeesAdapter = new WaitlistUsersAdapter(attendeesUsers, this, eventName);
        attendeesRecyclerView.setAdapter(attendeesAdapter);

        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
        ActivityResultLauncher<Intent> notificationActivityResultLauncher;
        ActivityResultLauncher<Intent> settingsResultLauncher;
        ActivityResultLauncher<Intent> MainActivityResultLauncher;
        // Fetch the waitlist from Firebase
        fetchattendeesList();

        //Grab private key from firestore for notifications.
        firestore.collection("private_key")
                .document("key")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        privateKey = documentSnapshot.getString("pkey");
                    }
                })
                .addOnFailureListener(e -> Log.e("KEY", "Error fetching private key", e));

        ImageButton backButton = findViewById(R.id.back);
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        notifyButton = findViewById(R.id.send_notification_button);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        notifyButton.setOnClickListener(v -> {
            if (attendeesIds != null) {
                String notifyDate = String.valueOf(System.currentTimeMillis());
                setNotifiedInFirestore(eventName, notifyDate);
            } else {
                Toast.makeText(this, "Cannot notify. Empty list of entrants.", Toast.LENGTH_SHORT).show();
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
                newIntent = new Intent(ViewAttendeesListActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(ViewAttendeesListActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(ViewAttendeesListActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(ViewAttendeesListActivity.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            }else if (item.getItemId() == R.id.nav_scan_qr) {
                Intent intent = new Intent(ViewAttendeesListActivity.this, QRScannerActivity.class);
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
     * Gets the attendees list from a specific event and calls fetchUserName on each device ID.
     */
    private void fetchattendeesList() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("attendees list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userDeviceId = document.getId();
                            attendeesIds.add(userDeviceId);
                            fetchUserName(userDeviceId); // Fetch the name for each userDeviceId
                        }
                    } else {
                        Log.e("WaitlistActivity", "Error getting waitlist: ", task.getException());
                    }
                });
    }

    //
    /**
     * Fetches the user's name and profile picture URL based on the device ID. Saves them to attendeesUsers
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
                            attendeesUsers.add(user);
                            attendeesAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitlistActivity", "Error fetching user data", e));
    }

    private void setNotifiedInFirestore(String eventName, String notifyDate) {
        for (int i = 0; i < attendeesIds.size(); i++) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notifyDate", notifyDate);
            String entrantId = attendeesIds.get(i);

            final String[] toggleNotif = {"none"};
            firestore.collection("entrants")
                    .document(entrantId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    toggleNotif[0] = documentSnapshot.getString("notifications");
                                }
                            });

            firestore.collection("entrants")
                    .document(entrantId)
                    .collection("Joined Events")
                    .document(eventName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firestoreNotifyDate = documentSnapshot.getString("notifyDate");
                            if (firestoreNotifyDate == null) {
                                firestore.collection("entrants")
                                        .document(entrantId)
                                        .collection("Joined Events")
                                        .document(eventName)
                                        .set(notificationData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Notify Users", "User set as notified in firestore");

                                            //Send push notification to specific user.
                                            notifyAttendingUser(entrantId, eventName, toggleNotif[0]);
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
        Toast.makeText(ViewAttendeesListActivity.this, "Previously un-notified users have been notified.", Toast.LENGTH_SHORT).show();
    }

    private void notifyAttendingUser(String invitedId, String eventName, String notificationToggle) {
        String invitedTitle = "Attending Event";
        String invitedBody = "Congrats! You have decided to attend the " + eventName + " event.";

        firestore.collection("profiles")
                .document(invitedId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String invitedToken = documentSnapshot.getString("notificationToken");

                        if (invitedToken != null) {
                            if (!Objects.equals(notificationToggle, "off")) {
                                FCMNotificationSender attendNotification = new FCMNotificationSender(invitedToken, invitedTitle, invitedBody, getApplicationContext());
                                attendNotification.SendNotifications(privateKey);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Notification", "Failed to send user push notification. ", e));
    }
}