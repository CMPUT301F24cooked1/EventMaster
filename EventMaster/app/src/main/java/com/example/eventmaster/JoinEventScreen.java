package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a recycler view of all the events that are created in EventMaster
 * Allows the entrant to click on any event
 */

public class JoinEventScreen extends AppCompatActivity {
    /**
     * Initializes the Join Event Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view all events and select any event
     */
    private RecyclerView recyclerView;
   // private EventAdapter eventAdapter;
    private ViewEventsAdapter ViewEventsAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
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
        setContentView(R.layout.view_events_screen);

        user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity


        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        ViewEventsAdapter = new ViewEventsAdapter(eventList, this, user, false);
        recyclerView.setAdapter(ViewEventsAdapter);
        // Retrieve events from Firestore
        retrieveEvents();

        // Initialize navigation buttons
        ImageButton notificationButton = findViewById(R.id.notifications);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton homeButton = findViewById(R.id.home_icon);
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

        // Set click listeners for navigation buttons on the bottom of the screen
        // sends you to profile screen
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(JoinEventScreen.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        // sends you to settings screen
        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(JoinEventScreen.this, SettingsScreen.class);
            newIntent.putExtra("User", user);
            settingsResultLauncher.launch(newIntent);
        });

        // sends you to a list of invited events that you accepted
        homeButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(JoinEventScreen.this, MainActivity.class);
            newIntent.putExtra("User", user);
            MainActivityResultLauncher.launch(newIntent);
        });
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(JoinEventScreen.this, Notifications.class);
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

    // for the recycler view
    private void retrieveEvents() {
        CollectionReference facilitiesRef = firestore.collection("facilities");

        facilitiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot facilityDoc : task.getResult()) {
                    String facilityId = facilityDoc.getId();  // get facility for each event

                    // retrieve events for each facility
                    CollectionReference eventsRef = facilityDoc.getReference().collection("My Events");

                    eventsRef.get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                Event event = eventDoc.toObject(Event.class);
                                event.setDeviceID(facilityId);  // set the facility name to be the device id
                                eventList.add(event);

                            }
                            ViewEventsAdapter.notifyDataSetChanged(); // notify the adapter of data changes
                            Log.d("JoinEventScreen", "Number of events: " + eventList.size());
                            Log.d("JoinEventScreen", "Device ID: " + deviceId);

                        } else {
                            Log.d("JoinEventScreen", "QuerySnapshot is null");
                        }
                    });

                }} else {
                Log.e("JoinEventScreen", "Error getting documents: ", task.getException());
            }
        });
    }
}


