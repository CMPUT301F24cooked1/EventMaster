package com.example.eventmaster;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ViewCreatedEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private Profile user;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;


    @Override
    protected void onResume() {
        super.onResume();
        retrieveEvents(); // Refresh events every time the activity is resumed
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.activity_view_created_events);
        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("User");
        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this, user);
        recyclerView.setAdapter(eventAdapter);

        // Retrieve events from Firestore
        retrieveEvents();
        // Initialize navigation buttons
        ImageButton notificationButton = findViewById(R.id.notifications);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton homeButton = findViewById(R.id.home_icon);
        ImageButton backButton = findViewById(R.id.back_button); // Initialize back button
        ImageButton addButton = findViewById(R.id.add_icon);

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


        // Set click listeners for navigation
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(ViewCreatedEventsActivity.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        // sends you to settings screen
        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(ViewCreatedEventsActivity.this, SettingsScreen.class);
            newIntent.putExtra("User", user);
            settingsResultLauncher.launch(newIntent);
        });

        // sends you to a list of invited events that you accepted
        homeButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(ViewCreatedEventsActivity.this, MainActivity.class);
            newIntent.putExtra("User", user);
            MainActivityResultLauncher.launch(newIntent);
        });
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(ViewCreatedEventsActivity.this, Notifications.class);
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

    private void retrieveEvents() {
        CollectionReference eventsRef = firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events");

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                Log.d("ViewCreatedEventsActivity", "QuerySnapshot count: " + querySnapshot.size());
                if (querySnapshot != null) {
                    eventList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                    }
                    eventAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                    Log.d("ViewCreatedEventsActivity", "Number of eveunts: " + eventList.size());
                    Log.d("ViewCreatedEventsActivity", "Device ID: " + deviceId);

                } else {
                    Log.d("ViewCreatedEventsActivity", "QuerySnapshot is null");
                }
            } else {
                Log.e("ViewCreatedEventsActivity", "Error getting documents: ", task.getException());
            }
        });
    }

}