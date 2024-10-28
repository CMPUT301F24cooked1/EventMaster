package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class JoinEventScreen extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private FirebaseFirestore db; // Firestore instance
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_events_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity



        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(eventAdapter);
        // Retrieve events from Firestore
        retrieveEvents();

//        // Set click listeners for navigation
//        notificationButton.setOnClickListener(v -> {
//            Intent intent = new Intent(ViewCreatedEventsActivity.this, Notifications.class);
//            startActivity(intent);
//        });
//
//        settingsButton.setOnClickListener(v -> {
//            Intent intent = new Intent(ViewCreatedEventsActivity.this, SettingsScreen.class);
//            startActivity(intent);
//        });
//
//        profileButton.setOnClickListener(v -> {
//            Intent intent = new Intent(ViewCreatedEventsActivity.this, ProfileActivity.class);
//            intent.putExtra("User", user);
//            startActivity(intent);
//        });
//
//        homeButton.setOnClickListener(v -> {
//            Intent intent = new Intent(ViewCreatedEventsActivity.this, MainActivity.class);
//            startActivity(intent);
//        });
//        // Set click listener for the back button
//        backButton.setOnClickListener(v -> {
//            finish(); // Close the current activity and return to the previous one
//        });


    }

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
                            eventAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
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


