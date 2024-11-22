package com.example.eventmaster;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The Notifications class represents the activity for displaying notifications within the app.
 * <p>
 * This activity connects to a layout resource file where notifications will be displayed to the user.
 * </p>
 */
public class Notifications extends AppCompatActivity {

    /**
     * Initializes the Notifications activity.
     * <p>
     * This method sets up the activity layout by associating it with the corresponding XML layout file.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after being shut down, this Bundle contains the data it most recently saved.
     */
    private RecyclerView recyclerView;
    private NotificationsAdapter NotificationsAdapter;
    private List<Event> inviteList;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private FirebaseFirestore db; // Firestore instance
    private TextView displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);

        // Connect the layout with the Java class
        setContentView(R.layout.notifications_screen); // Make sure the layout file is named correctly
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // todo: user from MainActivity

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Set up RecyclerView
        displayName = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inviteList = new ArrayList<>();
        eventList = new ArrayList<>();
        NotificationsAdapter = new NotificationsAdapter(eventList, this, user);
        recyclerView.setAdapter(NotificationsAdapter);
        // Retrieve events from Firestore
        retrieveNotifiedEvents(deviceId);
        retrieveRejectedEvents(deviceId, inviteList);

        Toast.makeText(this, "Device ID: " + deviceId, Toast.LENGTH_LONG).show();
    }



    private void retrieveNotifiedEvents(String entrantId) {

        DocumentReference entrantDocRef = firestore.collection("entrants").document(entrantId);

        // Retrieve waitlisted events for the specific entrant
        entrantDocRef.collection("Invited Events").get().addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                    // Create Event object and set fields
                    Event event = new Event();
                    event.setEventName(eventDoc.getId()); // Set the document name as eventName
                    inviteList.add(event);
//                    Event event = eventDoc.toObject(Event.class);
//                    event.setDeviceID(entrantId);  // Set the device ID as the entrant ID
//                    inviteList.add(event);
                }
                NotificationsAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                Log.d("JoinEventScreen", "Number of waitlisted events: " + inviteList.size());
                Log.d("JoinEventScreen", "Device ID: " + entrantId);

            } else {
                Log.d("Notifications", "QuerySnapshot is null");
            }
        }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving waitlisted events", e));
    }


    private void retrieveRejectedEvents(String entrantId, List<Event> inviteList) {

        firestore.collection("entrants")
                .document(entrantId)
                .collection("Rejected Events")
                .get()
                .addOnCompleteListener(rejectedTask -> {   // Retrieve waitlisted events for the specific entrant
            if (rejectedTask.isSuccessful()) {
                List<Event> rejectedList = new ArrayList<>();
                for (QueryDocumentSnapshot eventDoc : rejectedTask.getResult()) {
                    Event event = new Event();
                    event.setEventName(eventDoc.getId()); // Set the document name as eventName
                    rejectedList.add(event);
                }

                inviteList.addAll(rejectedList);
                eventList.addAll(inviteList);

                NotificationsAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                Log.d("JoinEventScreen", "Number of waitlisted events: " + inviteList.size());
                Log.d("JoinEventScreen", "Device ID: " + entrantId);


            } else {
                Log.d("Notifications", "QuerySnapshot is null");
            }
        }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving waitlisted events", e));
    }





}


