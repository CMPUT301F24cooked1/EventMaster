package com.example.eventmaster;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;

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


        // Set click listeners for navigation
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewCreatedEventsActivity.this, Notifications.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewCreatedEventsActivity.this, SettingsScreen.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewCreatedEventsActivity.this, ProfileActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewCreatedEventsActivity.this, MainActivity.class);
            startActivity(intent);
        });
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewCreatedEventsActivity.this, CreateEventActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            finish(); // Close the current activity and return to the previous one
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