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
import com.google.firebase.firestore.Query;
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
    private List<Event> attendeesList;
    private List<Event> waitlistList;
    private FirebaseFirestore firestore;
    private String deviceId;
    private FirebaseFirestore db;
    private TextView displayName;

    /**
     * Initializes the Notifications activity, sets up views, and retrieves event data from Firestore.
     *
     * @param savedInstanceState Saved instance state for restoring activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);

        setContentView(R.layout.notifications_screen);

        Profile user = (Profile) getIntent().getSerializableExtra("User");

        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        displayName = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inviteList = new ArrayList<>();
        eventList = new ArrayList<>();

        // Retrieve events from Firestore
        retrieveNotifiedEvents(deviceId);
        retrieveRejectedEvents(deviceId);
        retrieveAttendeesEvents(deviceId);
        retrieveWaitlistEvents(deviceId);

        notificationsAdapter = new NotificationsAdapter(eventList, this, user, (eventName, facilityID) -> {
            Intent intent = null;

            if (isEventInList(eventName, inviteList)) {
                intent = new Intent(this, NotificationInvitedActivity.class);
            } else if (isEventInList(eventName, rejectedList)) {
                intent = new Intent(this, NotificationRejectedActivity.class);
            }
            else if (isEventInList(eventName, attendeesList)) {
                intent = new Intent(this, NotificationAttendeesActivity.class);
            }
            else if (isEventInList(eventName, waitlistList)) {
                intent = new Intent(this, NotificationWaitlistActivity.class);
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

    /**
     * Checks if an event is in a given list.
     *
     * @param eventName Name of the event to check
     * @param list List of events to search in
     * @return True if the event is found, false otherwise
     */
    private boolean isEventInList(String eventName, List<Event> list) {
        for (Event event : list) {
            if (event.getEventName().equals(eventName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the list of invited events notifications from Firestore.
     *
     * @param entrantId The unique identifier for the entrant (device ID)
     */
    private void retrieveNotifiedEvents(String entrantId) {
        DocumentReference entrantDocRef = firestore.collection("entrants").document(entrantId);

        entrantDocRef.collection("Invited Events")
                .whereNotEqualTo("notifyDate", null) 
                .orderBy("notifyDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(eventTask -> {
                    if (eventTask.isSuccessful()) {
                        for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                            Event event = new Event();

                            // Retrieve and set the event name and facility ID
                            String eventName = eventDoc.getId();
                            String facilityID = eventDoc.getString("facilityId");
                            event.setEventName(eventName);
                            event.setDeviceID(facilityID);

                            event.setNotificationType("Invited"); 
                            inviteList.add(event); 
                        }

                        eventList.addAll(inviteList); 
                        notificationsAdapter.notifyDataSetChanged(); 
                        Log.d("JoinEventScreen", "Number of waitlisted events: " + inviteList.size());
                        Log.d("JoinEventScreen", "Device ID: " + entrantId);

                    } else {
                        Log.d("Notifications", "QuerySnapshot is null or no invited events found.");
                    }
                }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving invited events", e));

    }

    /**
     * Retrieves the list of rejected events notifications from Firestore.
     *
     * @param entrantId  The unique identifier for the entrant (device ID)
     */
    private void retrieveRejectedEvents(String entrantId) {
    firestore.collection("entrants")
            .document(entrantId)
            .collection("Rejected Events")
            .whereNotEqualTo("notifyDate", null)
            .orderBy("notifyDate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(rejectedTask -> {
                if (rejectedTask.isSuccessful()) {
                    rejectedList = new ArrayList<>();
                    for (QueryDocumentSnapshot eventDoc : rejectedTask.getResult()) {
                        Event event = new Event();

                        String eventName = eventDoc.getId();
                        String facilityID = eventDoc.getString("facilityId");

                        event.setEventName(eventName);
                        event.setDeviceID(facilityID); 
                        event.setNotificationType("Rejected"); 

                        rejectedList.add(event);
                    }

                    eventList.addAll(rejectedList);
                    notificationsAdapter.notifyDataSetChanged(); 

                    Log.d("JoinEventScreen", "Number of rejected events: " + rejectedList.size());
                    Log.d("JoinEventScreen", "Device ID: " + entrantId);
                } else {
                    Log.d("Notifications", "No rejected events found.");
                }
            }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving rejected events", e));
    }

    /**
     * Retrieves the list of attendees events notification from Firestore.
     *
     * @param entrantId  The unique identifier for the entrant (device ID)
     */
    private void retrieveAttendeesEvents(String entrantId) {
        firestore.collection("entrants")
                .document(entrantId)
                .collection("Attendees Events")
                .whereNotEqualTo("notifyDate", null)
                .orderBy("notifyDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(rejectedTask -> {
                    if (rejectedTask.isSuccessful()) {
                        rejectedList = new ArrayList<>();
                        for (QueryDocumentSnapshot eventDoc : rejectedTask.getResult()) {
                            Event event = new Event();

                            String eventName = eventDoc.getId();
                            String facilityID = eventDoc.getString("facilityId");

                            event.setEventName(eventName);
                            event.setDeviceID(facilityID);
                            event.setNotificationType("Attendees");

                            attendeesList.add(event);
                        }

                        eventList.addAll(attendeesList);
                        notificationsAdapter.notifyDataSetChanged();

                        Log.d("JoinEventScreen", "Number of rejected events: " + attendeesList.size());
                        Log.d("JoinEventScreen", "Device ID: " + entrantId);
                    } else {
                        Log.d("Notifications", "No attendees events found.");
                    }
                }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving attendees events", e));
    }

    /**
     * Retrieves the list of waitlist events notifications from Firestore.
     *
     * @param entrantId  The unique identifier for the entrant (device ID)
     */
    private void retrieveWaitlistEvents(String entrantId) {
        firestore.collection("entrants")
                .document(entrantId)
                .collection("Unsampled Events")
                .whereNotEqualTo("notifyDate", null)
                .orderBy("notifyDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(rejectedTask -> {
                    if (rejectedTask.isSuccessful()) {
                        rejectedList = new ArrayList<>();
                        for (QueryDocumentSnapshot eventDoc : rejectedTask.getResult()) {
                            Event event = new Event();

                            String eventName = eventDoc.getId();
                            String facilityID = eventDoc.getString("facilityId");

                            event.setEventName(eventName);
                            event.setDeviceID(facilityID);
                            event.setNotificationType("Waitlists");

                            waitlistList.add(event);
                        }

                        eventList.addAll(waitlistList);
                        notificationsAdapter.notifyDataSetChanged();

                        Log.d("JoinEventScreen", "Number of rejected events: " + waitlistList.size());
                        Log.d("JoinEventScreen", "Device ID: " + entrantId);
                    } else {
                        Log.d("Notifications", "No waitlist events found.");
                    }
                }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving waitlist events", e));
    }

}


