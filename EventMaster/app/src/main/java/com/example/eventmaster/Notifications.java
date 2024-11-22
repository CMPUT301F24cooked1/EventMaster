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

        setContentView(R.layout.notifications_screen); // Make sure the layout file is named correctly
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // todo: user from MainActivity

        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Set up the RecyclerView
        displayName = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inviteList = new ArrayList<>();
        eventList = new ArrayList<>();
        NotificationsAdapter = new NotificationsAdapter(eventList, this, user);
        recyclerView.setAdapter(NotificationsAdapter);

        // get events from the Firestore
        retrieveNotifiedEvents(deviceId);
        retrieveRejectedEvents(deviceId, inviteList);
    }


//    private void retrieveNotifiedEvents(String entrantId) {
//        DocumentReference entrantDocRef = firestore.collection("entrants").document(entrantId);
//
//        entrantDocRef.collection("Invited Events").whereGreaterThan("running", 0).get().addOnCompleteListener(eventTask -> {
//           // whereNotEqualTo("notifyDate", null)
//            // .orderBy("notifyDate", Query.Direction.DESCENDING)
//
//            if (eventTask.isSuccessful()) {
//                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
//                    Event event = eventDoc.toObject(Event.class);
//                    event.setEventName(eventDoc.getId()); // get event name
//                    event.setNotificationType("Invited"); // set notification type
//                    inviteList.add(event);   // add invited event to invited list
//                }
//                NotificationsAdapter.notifyDataSetChanged();
//            }
//        }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving invited events", e));
//    }
//
//    private void retrieveRejectedEvents(String entrantId, List<Event> inviteList) {
//        firestore.collection("entrants")
//                .document(entrantId)
//                .collection("Rejected Events")
//                .get()
//                .addOnCompleteListener(rejectedTask -> {
//                    if (rejectedTask.isSuccessful()) {
//                        List<Event> rejectedList = new ArrayList<>();
//                        for (QueryDocumentSnapshot eventDoc : rejectedTask.getResult()) {
//                            Event event = eventDoc.toObject(Event.class);
//                            event.setEventName(eventDoc.getId()); // get event name
//                            event.setNotificationType("Rejected"); // set notification type
//                            rejectedList.add(event);  // add rejected event to rejected list
//                        }
//                        inviteList.addAll(rejectedList);
//                        eventList.addAll(inviteList);  // add all events in one list to display everything
//                        NotificationsAdapter.notifyDataSetChanged();
//                    }
//                }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving rejected events", e));
//    }


    private void retrieveNotifiedEvents(String entrantId) {
        DocumentReference entrantDocRef = firestore.collection("entrants").document(entrantId);

        entrantDocRef.collection("Invited Events")
                .whereNotEqualTo("notifyDate", null)
                .orderBy("notifyDate", Query.Direction.DESCENDING)// Ensure only events with "running" field are retrieved
                .get()
                .addOnCompleteListener(eventTask -> {
                    if (eventTask.isSuccessful()) {
                        for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                            Event event = eventDoc.toObject(Event.class);
                            event.setEventName(eventDoc.getId()); // Get event name
                            event.setNotificationType("Invited"); // Set notification type
                            inviteList.add(event); // Add invited event to the list
                        }
                        eventList.addAll(inviteList); // Add invited events to the main list
                        NotificationsAdapter.notifyDataSetChanged(); // Refresh RecyclerView
                    } else {
                        Log.d("Notifications", "No invited events found.");
                    }
                }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving invited events", e));
    }

    private void retrieveRejectedEvents(String entrantId, List<Event> inviteList) {
        firestore.collection("entrants")
                .document(entrantId)
                .collection("Rejected Events")
                .whereNotEqualTo("notifyDate", null)
                .orderBy("notifyDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(rejectedTask -> {
                    if (rejectedTask.isSuccessful()) {
                        List<Event> rejectedList = new ArrayList<>();
                        for (QueryDocumentSnapshot eventDoc : rejectedTask.getResult()) {
                            // Log details for debugging
                            Log.d("DebugRejectedEvent", "Event Name: " + eventDoc.getId() + ", Notify Date: " + eventDoc.get("notifyDate"));

                            Event event = eventDoc.toObject(Event.class);
                            event.setEventName(eventDoc.getId()); // Get event name
                            event.setNotificationType("Rejected"); // Set notification type
                            rejectedList.add(event); // Add rejected event to the list
                        }
                        //inviteList.addAll(rejectedList); // Add rejected events to the invited list
                        eventList.addAll(rejectedList); // Combine all events into the main list
                        NotificationsAdapter.notifyDataSetChanged(); // Refresh RecyclerView
                    } else {
                        Log.d("Notifications", "No rejected events found.");
                    }
                }).addOnFailureListener(e -> Log.e("Notifications", "Error retrieving rejected events", e));
    }


}


