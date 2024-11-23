package com.example.eventmaster;

import android.content.Intent;
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
    private NotificationsAdapter notificationsAdapter;
    private List<Event> inviteList;
    private List<Event> eventList;
    private List<Event> rejectedList;
    private FirebaseFirestore firestore;
    private String deviceId;
    private FirebaseFirestore db;
    private TextView displayName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);


        setContentView(R.layout.notifications_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // todo: user from MainActivity

        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        displayName = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inviteList = new ArrayList<>();
        eventList = new ArrayList<>();


        // Retrieve events from Firestore
        retrieveNotifiedEvents(deviceId);
        retrieveRejectedEvents(deviceId, inviteList);

        notificationsAdapter = new NotificationsAdapter(eventList, this, user, (eventName, facilityID) -> {
            Intent intent = null;

            if (isEventInList(eventName, inviteList)) {
                intent = new Intent(this, NotificationInvitedActivity.class);
            } else if (isEventInList(eventName, rejectedList)) {
                intent = new Intent(this, NotificationRejectedActivity.class);
            }

            if (intent != null) {
                intent.putExtra("event_name", eventName);
                intent.putExtra("facility_id", facilityID);
                intent.putExtra("User", user);

                Log.d("Intent", "Passing Event Name: " + eventName);
                Log.d("Intent", "Passing Facility ID: " + facilityID);

                startActivity(intent);
            } else {
                Toast.makeText(this, "Error: Unable to determine event type", Toast.LENGTH_SHORT).show();
                Log.e("Notifications", "Event not found in either invite or rejected list.");
            }
        });


        recyclerView.setAdapter(notificationsAdapter);

        Toast.makeText(this, "Device ID: " + deviceId, Toast.LENGTH_LONG).show();

    }

    // checks which event is in which list
    private boolean isEventInList(String eventName, List<Event> list) {
        for (Event event : list) {
            if (event.getEventName().equals(eventName)) {
                return true;
            }
        }
        return false;
    }

    private void retrieveNotifiedEvents(String entrantId) {

        DocumentReference entrantDocRef = firestore.collection("entrants").document(entrantId);

        // Retrieve waitlisted events for the specific entrant
        entrantDocRef.collection("Invited Events").get().addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                    // Create Event object and set fields
                    Event event = new Event();
                    String eventName = eventDoc.getId();
                    String facilityID = eventDoc.getString("facilityId");
                    event.setEventName(eventDoc.getId()); // Set the document name as eventName
                    event.setDeviceID(eventDoc.getString("facilityId"));
                    inviteList.add(event);
//                    Event event = eventDoc.toObject(Event.class);
//                    event.setDeviceID(entrantId);  // Set the device ID as the entrant ID
//                    inviteList.add(event);
                }
                notificationsAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
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
                rejectedList = new ArrayList<>();
                for (QueryDocumentSnapshot eventDoc : rejectedTask.getResult()) {
                    Event event = new Event();
                    String eventName = eventDoc.getId();
                    String facilityID = eventDoc.getString("facilityId");
                    event.setEventName(eventDoc.getId()); // Set the document name as eventName
                    event.setDeviceID(eventDoc.getString("facilityId"));
                    rejectedList.add(event);
                }

                inviteList.addAll(rejectedList);
                eventList.addAll(inviteList);

                notificationsAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                Log.d("JoinEventScreen", "Number of waitlisted events: " + inviteList.size());
                Log.d("JoinEventScreen", "Device ID: " + entrantId);


            } else {
                Log.d("Notifications", "QuerySnapshot is null");
            }
        }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving waitlisted events", e));
    }





}


