package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
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

// https://www.c-sharpcorner.com/article/android-qr-code-bar-code-scanner/
public class WaitlistedEventsActivity extends AppCompatActivity {
    /**
     * Initializes the Join Event Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view all events and select any event
     */
    private RecyclerView recyclerView;
    private WaitlistedEventsAdapter WaitlistedEventsAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private FirebaseFirestore db; // Firestore instance

    //  private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.waitlisted_events_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity



        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();

        WaitlistedEventsAdapter = new WaitlistedEventsAdapter(eventList, this);
        recyclerView.setAdapter(WaitlistedEventsAdapter);

        // Retrieve events from Firestore
        retrieveWaitlistedEvents();

    }


    /**
     * Retrieve waitlisted events for user
     */
    private void retrieveWaitlistedEvents() {
        CollectionReference entrantsRef = firestore.collection("entrants");

        entrantsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot entrantDoc : task.getResult()) {
                    String entrantId = entrantDoc.getId();  // get entrant for each waitlist
                    //Toast.makeText(JoinEventScreen.this, "Facility ID: " + facilityId, Toast.LENGTH_SHORT).show();

                    // retrieve events for each facility
                    CollectionReference eventsRef = entrantDoc.getReference().collection("Waitlisted Events");

                    eventsRef.get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                Event event = eventDoc.toObject(Event.class);
                                event.setDeviceID(entrantId);  // set the facility name to be the device id
                                eventList.add(event);


                            }
                            WaitlistedEventsAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                            Log.d("JoinEventScreen", "Number of waitlisted events: " + eventList.size());
                            Log.d("JoinEventScreen", "Device ID: " + deviceId);

                        } else {
                            Log.d("WaitlistedEventsActivity", "QuerySnapshot is null");
                        }
                    });

                }} else {
                Log.e("WaitlistedEventsActivity", "Error getting documents: ", task.getException());
            }
        });
    }



    // Reference to the settings button


//    public void onEventClick(Event event) {
//        // Start QR scanner activity
//        Intent intent = new Intent(this, QRScanFragment.class);
//        startActivity(intent);
//    }



}


