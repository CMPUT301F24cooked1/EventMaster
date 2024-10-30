package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private FirebaseFirestore db;
    TextView eventName;
    TextView eventDescription;
    TextView eventFinalDate;
    ImageView eventPoster;

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
        String hashedData = intent.getStringExtra("HASHED_DATA");
        String deviceID = intent.getStringExtra("deviceID");  // facility id
        String event = intent.getStringExtra("event");


        // Ensure the received data is not null
        if (hashedData != null && deviceID != null) {
            retrieveEventInfo(hashedData, deviceID, event);

        } else {
            Toast.makeText(this, "Failed to retrieve event data.", Toast.LENGTH_SHORT).show();
        }


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
                                displayEventInfo(eventName, eventDescription, eventPosterUrl);
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

