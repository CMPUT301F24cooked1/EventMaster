package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class retrieveEventInfo extends AppCompatActivity {
    private FirebaseFirestore db;
    TextView eventName;
    TextView eventDescription;
    TextView eventFinalDate;
    ImageView eventPoster;
    ImageButton notificationButton;
    ImageButton settingsButton;
    ImageButton profileButton;
    ImageButton listButton;
    AppCompatButton joinWaitlistButton;
    ActivityResultLauncher<Intent> joinWaitlistResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_screen);
        eventName = findViewById(R.id.event_name);
        eventDescription = findViewById(R.id.event_decription);
        eventFinalDate = findViewById(R.id.event_decription);
        eventPoster = findViewById(R.id.event_poster);
        joinWaitlistButton = findViewById(R.id.join_waitlist_button);

        db = FirebaseFirestore.getInstance();

        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        // retrieve information after scanning
        Intent intent = getIntent();
        String hashedData = intent.getStringExtra("hashed_data");
        String deviceID = intent.getStringExtra("deviceID");  // facility id
        String event = intent.getStringExtra("event");
        String posterUrl = intent.getStringExtra("posterUrl");

        String userDeviceID = "b61f150a76cf9176";

        Intent intent2 = new Intent(retrieveEventInfo.this, JoinWaitlistScreen.class);
        intent2.putExtra("hashed_data", hashedData);
        intent2.putExtra("deviceID", deviceID);
        intent2.putExtra("event", event);
        intent2.putExtra("posterUrl", posterUrl);


        // Ensure the received data is not null
        if (hashedData != null && deviceID != null) {
            retrieveEventInfo(hashedData, deviceID, event);
        } else {
            Toast.makeText(this, "Failed to retrieve event data.", Toast.LENGTH_SHORT).show();
        }




        joinWaitlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(retrieveEventInfo.this, JoinWaitlistScreen.class);
                intent.putExtra("User", user);
                fetchEventData(hashedData, deviceID, event, posterUrl);
                joinWaitlist(userDeviceID);
            }
        });



        // Bottom Bar TODO: Complete for this class and the settings screen and the join events screen
//        notificationButton = findViewById(R.id.notification_icon);
//        notificationButton.setOnClickListener(v -> {
//            Intent intent1 = new Intent(retrieveEventInfo.this, Notifications.class);
//            startActivity(intent1);
//        });
//
//        settingsButton = findViewById(R.id.settings);
//        settingsButton.setOnClickListener(v -> {
//            Intent intent2 = new Intent(retrieveEventInfo.this, SettingsScreen.class);
//            startActivity(intent2);
//        });
//
//        profileButton = findViewById(R.id.profile);
//        profileButton.setOnClickListener(v -> {
//            Intent intent3 = new Intent(retrieveEventInfo.this, ProfileActivity.class);
//            startActivity(intent3);
//        });
//
//        listButton = findViewById(R.id.list_icon);
//        listButton.setOnClickListener(v -> {
//            Intent intent4 = new Intent(retrieveEventInfo.this, ViewCreatedEventsActivity.class);
//            startActivity(intent4);
//        });


    }

    private void retrieveEventInfo(String hashedData, String deviceID, String event) {
        db.collection("facilities")
                .document(deviceID)
                .collection("My Events")
                .whereEqualTo("hash", hashedData)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        if (!task.getResult().isEmpty()){  // check something is in result
                            Toast.makeText(retrieveEventInfo.this, "succesfully read data", Toast.LENGTH_SHORT).show();

                            for (DocumentSnapshot document : task.getResult()) {
                                // Retrieve data directly from the document
                                String eventName = document.getString("eventName");
                                String eventDescription = document.getString("eventDescription");
                                String eventPosterUrl = document.getString("posterUrl");

                                // Call the display method with the retrieved data
                                displayEventInfo(eventName, eventDescription, eventPosterUrl);//eventPosterUrl);
                            }
                    } else {
                        Toast.makeText(retrieveEventInfo.this, "Event does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(retrieveEventInfo.this, "Error retrieving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayEventInfo(String eventName, String eventDescription, String eventPosterUrl ) {
        TextView eventNameTextView = findViewById(R.id.event_name);
        TextView eventDescriptionTextView = findViewById(R.id.event_decription);

        // Set the text for the TextViews
        eventNameTextView.setText(eventName);
        eventDescriptionTextView.setText(eventDescription);

        // upload the image from the firebase
        try {
            Glide.with(this)
                    .load(eventPosterUrl)
                    .into(eventPoster);
        } catch (Exception e) {
            Log.e("RetrieveEventInfo", "Error loading image: " + e.getMessage());
        }
    }

    private void fetchEventData(String hashedData, String deviceID, String event, String posterUrl) {
        Intent intent2 = new Intent(retrieveEventInfo.this, JoinWaitlistScreen.class);
        intent2.putExtra("hashed_data", hashedData);
        intent2.putExtra("deviceID", deviceID);
        intent2.putExtra("event", event);
        intent2.putExtra("posterUrl", posterUrl);

        startActivity(intent2);

    }


    /*private void joinWaitlist(String userDeviceID, String path) {
        db.collection(path).document(userDeviceID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // checks if deviceID is already in Firestore
                        if (task.getResult().exists()) {
                            String existingEventName = (String) task.getResult().get("Waitlisted events");
                            if (existingEventName != null && existingEventName.contains(eventName.toString())) {
                                Log.d("Firestore", "Event name already exists, skipping insertion.");
                            }
                            } else {
                                Map<String, Object> deviceData = new HashMap<>();
                                deviceData.put("Waitlisted events", eventName.toString());

                                db.collection(path).document(userDeviceID).set(deviceData) // document is deviceID
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Entrant successfully joined waitlist");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error joining waitlist", e);
                                        });
                            }
                        }else {
                        Log.e("Firestore", "Error checking if device ID exists", task.getException());
                    }
                });

    }*/



    private void joinWaitlist(String userDeviceID) {
        Map<String, Object> WaitlistEvents = new HashMap<>();
        WaitlistEvents.put("Waitlisted events", eventName);

        db.collection("entrants")
                .document(userDeviceID)
                .set(WaitlistEvents, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Entrant successfully joined waitlist.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error joining waitlist", e);
                });
    }
}

