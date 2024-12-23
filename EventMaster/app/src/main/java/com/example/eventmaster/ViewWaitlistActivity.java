package com.example.eventmaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ViewWaitlistActivity is responsible for displaying the waitlist for a specific event in a Firebase-backed application.
 * It provides functionality to fetch the list of users on the waitlist, sample a subset of these users,
 * and then add them to the invited list for the event. The activity also allows the user to navigate to
 * different sections of the app like notifications, settings, profile, and home.
 */

public class ViewWaitlistActivity extends AppCompatActivity {

    private RecyclerView waitlistRecyclerView;
    private WaitlistUsersAdapter waitlistAdapter;
    private List<WaitlistUsersAdapter.User> waitlistUsers;
    private List<String> waitlistIds;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private String eventName; // Define event name or ID
    private Profile user;
    private String privateKey;

    private ActivityResultLauncher<Intent> chooseSampleResultLauncher;
    private AppCompatButton chooseSampleButton;
    private AppCompatButton notifyButton;


    /**
     * Called when the activity is first created. Initializes the view, sets up the RecyclerView,
     * fetches the waitlist data, and sets the click listeners for the UI components.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_waitlist);
        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("User");
        // Retrieve eventName from the Intent
        eventName = getIntent().getStringExtra("eventName");

        waitlistRecyclerView = findViewById(R.id.waitlistRecyclerView);
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        waitlistUsers = new ArrayList<>();
        waitlistIds = new ArrayList<>();

        // Pass the context and eventName to the adapter
        waitlistAdapter = new WaitlistUsersAdapter(waitlistUsers, this, eventName);
        waitlistRecyclerView.setAdapter(waitlistAdapter);

        firestore = FirebaseFirestore.getInstance();
        deviceId = user.getDeviceId();
        //deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Fetch the waitlist from Firebase
        fetchWaitlist();

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

        ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
        ActivityResultLauncher<Intent> notificationActivityResultLauncher;
        ActivityResultLauncher<Intent> settingsResultLauncher;
        ActivityResultLauncher<Intent> MainActivityResultLauncher;
        chooseSampleResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult returnResult) {
                if (returnResult != null && returnResult.getResultCode() == RESULT_OK) {
                    if (returnResult.getData() != null && returnResult.getData().getSerializableExtra("editedUserFacility") != null) {

                    }
                }
            }
        });

        notifyButton = findViewById(R.id.send_notification_button);
        notifyButton.setOnClickListener(v -> {
            if (waitlistIds != null) {
                String notifyDate = String.valueOf(System.currentTimeMillis());
                setNotifiedInFirestore(eventName, notifyDate);
            } else {
                Toast.makeText(this, "Cannot notify. Empty list of entrants.", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton backButton = findViewById(R.id.back);
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        ImageButton mapIconButton = findViewById(R.id.map_icon);
        mapIconButton.setOnClickListener(v -> checkGeolocationStatus());

        //Start sampling selected entrants in order to move to ViewInvitedListActivity
        chooseSampleButton = findViewById(R.id.choose_sample_button);
        chooseSampleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long waitlistSize = waitlistUsers.size();
                //Check if there are entrants in the waitlist
                if (waitlistSize == 0) {
                    Toast.makeText(ViewWaitlistActivity.this, "Waitlist does not have any entrants and thus cannot be sampled.", Toast.LENGTH_SHORT).show();
                    Log.d("WaitlistSize", "Waitlist size is 0, cannot sample.");
                    Intent intent = new Intent(ViewWaitlistActivity.this, ViewInvitedListActivity.class);
                    intent.putExtra("myEventName", eventName);
                    intent.putExtra("User", user);
                    intent.putExtra("Sampled?", 0);
                    chooseSampleResultLauncher.launch(intent);
                } else {
                    Log.d("WaitlistSize", "Waitlist size is " + waitlistSize + ": Sampling beginning now.");
                    DocumentReference docRef = firestore.collection("facilities")
                            .document(deviceId)
                            .collection("My Events")
                            .document(eventName);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    Map<String, Object> data = document.getData();

                                    if (data.get("selectedCount") == data.get("eventCapacity")) {
                                        Intent intent = new Intent(ViewWaitlistActivity.this, ViewInvitedListActivity.class);
                                        intent.putExtra("myEventName", eventName);
                                        intent.putExtra("User", user);
                                        intent.putExtra("Sampled?", 0);
                                        chooseSampleResultLauncher.launch(intent);
                                    } else {
                                        findSampleSize(eventName, waitlistSize);
                                    }
                                }
                            }
                        }
                    });
                }
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
                newIntent = new Intent(ViewWaitlistActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(ViewWaitlistActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(ViewWaitlistActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(ViewWaitlistActivity.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            }else if (item.getItemId() == R.id.nav_scan_qr) {
                Intent intent = new Intent(ViewWaitlistActivity.this, QRScannerActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });



    }
    private void checkGeolocationStatus() {
        DocumentReference eventRef = firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Boolean geolocationEnabled = document.getBoolean("geolocationEnabled");
                    if (geolocationEnabled != null && geolocationEnabled) {
                        openMapScreen();
                    } else {
                        Toast.makeText(this, "Event's geolocation is disabled.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Event data not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("GeolocationCheck", "Failed to fetch event data", task.getException());
            }
        });
    }
    private void openMapScreen() {
        Intent intent = new Intent(ViewWaitlistActivity.this, MapActivity.class);
        intent.putExtra("deviceId", deviceId);
        intent.putExtra("eventName", eventName);
        startActivity(intent);
    }

    private void openQRScanFragment() {
        // Open QRScanFragment without simulating button click
        Intent intent = new Intent(this, QRScanFragment.class);
        intent.putExtra("User", user);  // Pass the user information if needed
        startActivity(intent);

    }


    /**
     * Fetches the list of users from Firestore who are on the waitlist for the current event.
     * This method retrieves the user data and updates the waitlist RecyclerView adapter.
     */
    private void fetchWaitlist() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("unsampled list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userDeviceId = document.getId();
                            waitlistIds.add(userDeviceId);
                            fetchUserName(userDeviceId); // Fetch the name for each userDeviceId
                        }
                    } else {
                        Log.e("WaitlistActivity", "Error getting waitlist: ", task.getException());
                    }
                });
    }

    /**
     * Fetches the name and profile picture URL of a user from Firestore given their device ID.
     * Adds the user to the waitlist displayed in the UI.
     *
     * @param userDeviceId The device ID of the user whose information is being fetched.
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
                            waitlistUsers.add(user);
                            waitlistAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitlistActivity", "Error fetching user data", e));
    }

    /**
     * finds the provided sample size for the given event.
     * If the waitlist has less people than the sample size, it takes that as the selected count instead.
     * Calls sampleEntrants at the end.
     * @param eventName the name of the event selected
     * @param waitlistSize the size of the waitlist in the selected event
     */
    private void findSampleSize(String eventName, long waitlistSize) {

        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        //Get sample size from event in firestore
                        long sampleSize = (long) documentSnapshot.get("eventCapacity");
                        long selectedCount = 0;

                        //Get the number of entrants who have already been invited.
                        if (documentSnapshot.get("selectedCount") != null) {
                            selectedCount = (long) documentSnapshot.get("selectedCount");
                        }

                        //Check if the waitlist is smaller than the sample size, and assign waitlistSize accordingly
                        if ((sampleSize - selectedCount) > waitlistSize) {
                            sampleSize = waitlistSize + selectedCount;
                        }

                        //Set the new selectedCount value to full in Firestore
                        Map<String, Object> selectedCountData = new HashMap<>();
                        selectedCountData.put("selectedCount", sampleSize);
                        long finalSelectedCount = selectedCount;
                        long finalSampleSize = sampleSize;
                        firestore.collection("facilities")
                                .document(deviceId)
                                .collection("My Events")
                                .document(eventName)
                                .set(selectedCountData, SetOptions.merge()) // Merge with existing data
                                .addOnSuccessListener(aVoid -> {

                                    Log.d("Firestore", "Selected count updated successfully.");

                                    //Sample the specified number of entrants.
                                    sampleEntrants(eventName, finalSampleSize, finalSelectedCount);

                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error updating Selected count", e);
                                });
                    }
                });
    }

    /**
     * Randomly samples a selected number of entrants for an event
     * Calls updateInvitedList and updateUnsampledList
     * @param eventName the name of the event selected
     * @param sampleSize the size of the waitlist in the selected event
     * @param selectedCount the number of people who have already been selected for the event (and have not cancelled)
     */
    private void sampleEntrants(String eventName, long sampleSize, long selectedCount) {

        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("unsampled list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //Create new ArrayLists to hold deviceIDs of entrants
                        ArrayList<String> shuffleWaitlistIds = new ArrayList<>();
                        ArrayList<String> shuffledIds = new ArrayList<>();

                        //Add all waitlist IDs to shuffleWaitlistIds
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            shuffleWaitlistIds.add(document.getId());
                        }

                        //Shuffle and add the first (sampleSize - selectedCount) shuffled values into shuffledIds where
                        Collections.shuffle(shuffleWaitlistIds);
                        for (int i = 0; i < (sampleSize - selectedCount); i++) {
                            shuffledIds.add(shuffleWaitlistIds.get(i));
                        }

                        //Start updating all Firestore data.
                        updateInvitedList(shuffledIds, eventName);
                        updateUnsampledList(shuffledIds, eventName);
                    }

                });

    }

    /**
     * updates the invited list under the specified event in firestore.
     * Calls updateInvitedEvents for each ID as well
     * @param shuffledIds the list of device IDs previously shuffled in the waitlist.
     * @param eventName The name of the event selected.
     */
    private void updateInvitedList(ArrayList<String> shuffledIds, String eventName) {

        Map<String, Object> invitedData = new HashMap<>();
        invitedData.put("eventId", eventName);

        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("invited list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (int i = 0; i < shuffledIds.size(); i++) {
                            //Add all sampled IDs to Firestore under the invited list in the Event
                            String entrantId = shuffledIds.get(i);
                            firestore.collection("facilities")
                                    .document(deviceId)
                                    .collection("My Events")
                                    .document(eventName)
                                    .collection("invited list")
                                    .document(entrantId)
                                    .set(invitedData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Device ID field updated in invited list successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error updating device ID field in invited list", e);
                                    });

                            //Update info in Entrants in Firestore
                            updateInvitedEvents(entrantId, eventName);
                        }
                        //markEventAsSampled(deviceId, eventName);
                        Intent intent = new Intent(ViewWaitlistActivity.this, ViewInvitedListActivity.class);
                        intent.putExtra("myEventName", eventName);
                        intent.putExtra("User", user);
                        intent.putExtra("Sampled?", 1);
                        chooseSampleResultLauncher.launch(intent);
                    } else {
                        Log.e("WaitlistActivity", "Failed to find invited list", task.getException());
                    }
                });
    }

    /**
     * updates the unsampled list under the specified event in firestore by removing invited users.
     * Calls updateUnsampledEvents for each ID as well
     * @param shuffledIds the list of device IDs previously shuffled in the waitlist.
     * @param eventName The name of the event selected.
     */
    private void updateUnsampledList(ArrayList<String> shuffledIds, String eventName) {

        firestore.collection("facilities")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        for (int i = 0; i < shuffledIds.size(); i++) {
                            String entrantId = shuffledIds.get(i);
                            firestore.collection("facilities")
                                    .document(deviceId)
                                    .collection("My Events")
                                    .document(eventName)
                                    .collection("unsampled list")
                                    .document(entrantId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Device ID field updated in unsampled list successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error updating device ID field in unsampled list", e);
                                    });
                            updateUnsampledEvents(entrantId, eventName);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating device ID field in unsampled list", e);
                });
    }

    /**
     * Puts a selected event under a given entrant's Invited Events list in Firestore.
     * @param entrantId The given entrant's device ID to mark as invited in Firestore
     * @param eventName The name of the event selected.
     */
    private void updateInvitedEvents(String entrantId, String eventName) {
        Map<String, Object> invitedEntrantData = new HashMap<>();
        invitedEntrantData.put("Event Name", eventName);
        invitedEntrantData.put("facilityId", deviceId);

        //Add Event name to Entrant in Firestore under Invited Events
        firestore.collection("entrants")
                .document(entrantId)
                .collection("Invited Events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firestore.collection("entrants")
                                .document(entrantId)
                                .collection("Invited Events")
                                .document(eventName)
                                .set(invitedEntrantData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Invited Events", "Device ID field updated in entrant's invited list successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Invited Events", "Error updating device ID field in invited list", e);
                                });
                    } else {
                        Log.e("Invited Events", "Failed to add user to invited events", task.getException());
                    }
                });
    }

    /**
     * Puts a selected event under a given entrant's Unsampled Events list in Firestore.
     * @param entrantId The given entrant's device ID to mark as invited in Firestore
     * @param eventName The name of the event selected.
     */
    private void updateUnsampledEvents (String entrantId, String eventName) {
        firestore.collection("entrants")
                .document(entrantId)
                .collection("Unsampled Events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firestore.collection("entrants")
                                .document(entrantId)
                                .collection("Unsampled Events")
                                .document(eventName)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Invited Events", "Device ID field updated in entrant's invited list successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Invited Events", "Error updating device ID field in invited list", e);
                                });
                    } else {
                        Log.e("Invited Events", "Failed to add user to invited events", task.getException());
                    }
                });
    }

    /**
     * Runs through user Ids of the unsampled list and sets their notifyDates in firestore for later retrieval
     * Calls notifyUnsampledUser for each ID as well
     * @param eventName The name of the event selected.
     * @param notifyDate The date and time the notify button was pressed
     */
    private void setNotifiedInFirestore(String eventName, String notifyDate) {
        for (int i = 0; i < waitlistIds.size(); i++) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notifyDate", notifyDate);
            String entrantId = waitlistIds.get(i);

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
                    .collection("Unsampled Events")
                    .document(eventName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firestoreNotifyDate = documentSnapshot.getString("notifyDate");
                            if (firestoreNotifyDate == null) {
                                firestore.collection("entrants")
                                        .document(entrantId)
                                        .collection("Unsampled Events")
                                        .document(eventName)
                                        .set(notificationData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Notify Users", "User set as notified in firestore");

                                            //Send push notification to specific user.
                                            notifyUnsampledUser(entrantId, eventName, toggleNotif[0]);
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
        Toast.makeText(ViewWaitlistActivity.this, "Previously un-notified users have been notified.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Sends notification to an unsampled user with the set notification title and body
     * @param unsampledId The Id of the user to be notified
     * @param eventName The name of the event selected.
     * @param notificationToggle Whether the selected user has toggled notifications off
     */
    private void notifyUnsampledUser(String unsampledId, String eventName, String notificationToggle) {
        String unsampledTitle = "Waitlisted Event";
        String unsampledBody = "You have not yet been sampled for the " + eventName + " event, and still may be selected.";

        firestore.collection("profiles")
                .document(unsampledId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String unsampledToken = documentSnapshot.getString("notificationToken");

                        if (unsampledToken != null) {
                            if (!Objects.equals(notificationToggle, "off")) {
                                FCMNotificationSender unsampledNotification = new FCMNotificationSender(unsampledToken, unsampledTitle, unsampledBody, getApplicationContext());
                                unsampledNotification.SendNotifications(privateKey);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Notification", "Failed to send user push notification. ", e));
    }
}
