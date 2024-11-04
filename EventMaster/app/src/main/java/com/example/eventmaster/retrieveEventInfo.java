package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class retrieveEventInfo extends AppCompatActivity {
    /**
     * Initializes the Event Information Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * @param hashedData the hash data from the firebase that a QR code is created by
     * @param deviceID the facilities device ID
     * @param event the event // dont think I need this
     * Displays the event information of the chosen event
     */
    private FirebaseFirestore db;
    TextView eventName;
    TextView eventDescription;
    TextView eventFinalDate;
    ImageView eventPoster;
    ImageButton notificationButton;
    ImageButton settingsButton;
    ImageButton profileButton;
    ImageButton listButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details_screen);
        eventName = findViewById(R.id.event_name);
        eventDescription = findViewById(R.id.event_decription);
        eventFinalDate = findViewById(R.id.event_decription);
        eventPoster = findViewById(R.id.event_poster);

        db = FirebaseFirestore.getInstance();

        // retrieve information after scanning
        Intent intent = getIntent();
        String hashedData = intent.getStringExtra("hashed_data");
        String deviceID = intent.getStringExtra("deviceID");  // facility id
        String event = intent.getStringExtra("event");
        String posterUrl = intent.getStringExtra("posterUrl");


        // Ensure the received data is not null
        if (hashedData != null && deviceID != null) {
            retrieveEventInfo(hashedData, deviceID, event);

        } else {
            Toast.makeText(this, "Failed to retrieve event data.", Toast.LENGTH_SHORT).show();
        }


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





}

