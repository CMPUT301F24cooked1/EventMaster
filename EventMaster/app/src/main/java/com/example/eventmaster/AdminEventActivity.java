package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays all of the events that have been created
 */
public class AdminEventActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViewEventsAdapter ViewEventsAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private Button deleteButton;
    private boolean isDeleteMode = false;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;


    //  private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.admin_event_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        ViewEventsAdapter = new ViewEventsAdapter(eventList, this, user, true);
        recyclerView.setAdapter(ViewEventsAdapter);

        deleteButton = findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(v -> {
            if (!isDeleteMode) {
                ViewEventsAdapter.toggleCheckBoxVisibility();
                ViewEventsAdapter.notifyDataSetChanged();
                deleteButton.setText("Delete Items");
            } else {
                //delete checked items
                ViewEventsAdapter.deleteSelectedEvents();
                ViewEventsAdapter.toggleCheckBoxVisibility();
                ViewEventsAdapter.notifyDataSetChanged();
                deleteButton.setText("Select to Delete");
            }
            isDeleteMode = !isDeleteMode; // Toggle mode

        });

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
                result -> {});

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        notificationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        ProfileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        // Set click listeners for navigation buttons on the bottom of the screen
        // sends you to profile screen
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminEventActivity.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        // sends you to settings screen
        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminEventActivity.this, SettingsScreen.class);
            newIntent.putExtra("User", user);
            settingsResultLauncher.launch(newIntent);
        });

        // sends you to a list of invited events that you accepted
        homeButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminEventActivity.this, MainActivity.class);
            newIntent.putExtra("User", user);
            MainActivityResultLauncher.launch(newIntent);
        });
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminEventActivity.this, Notifications.class);
            newIntent.putExtra("User", user);
            notificationActivityResultLauncher.launch(newIntent);
        });

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            finish(); // Close the current activity and return to the previous one
        });
    }


    // for the recycler view
    private void retrieveEvents() {
        CollectionReference facilitiesRef = firestore.collection("facilities");

        facilitiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot facilityDoc : task.getResult()) {

                    // retrieve events for each facility
                    CollectionReference eventsRef = facilityDoc.getReference().collection("My Events");

                    eventsRef.get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                Event event = eventDoc.toObject(Event.class);
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
