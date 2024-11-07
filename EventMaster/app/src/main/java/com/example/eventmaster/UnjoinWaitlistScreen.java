package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UnjoinWaitlistScreen extends AppCompatActivity {

    private FirebaseFirestore db;
    TextView eventName;
    TextView eventDescription;
    TextView eventFinalDate;
    ImageView eventPoster;
    ImageButton notificationButton;
    ImageButton settingsButton;
    ImageButton profileButton;
    ImageButton listButton;
    ImageButton backButton;
    AppCompatButton unjoinWaitlistButton;

    //TODO: Update event name and description with corresponding details, update firestore when entrant unjoins waitlist
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.waitlist_event_details);

        eventName = findViewById(R.id.event_name);
        eventDescription = findViewById(R.id.event_decription);
        eventFinalDate = findViewById(R.id.event_decription);
        eventPoster = findViewById(R.id.event_poster);
        unjoinWaitlistButton = findViewById(R.id.unjoin_waitlist_button);

        db = FirebaseFirestore.getInstance();

        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        String userDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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

        unjoinWaitlistButton.setOnClickListener(new View.OnClickListener() { //handles the unjoining of entrant from event
            @Override
            public void onClick(View v) {
                unjoinWaitlistEntrant(userDeviceId, hashedData);
                unjoinWaitlistOrganizer(userDeviceId, deviceID);
            }
        });

        ImageButton notificationButton = findViewById(R.id.notification_icon);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton listButton = findViewById(R.id.list_icon);
        //ImageButton backButton = findViewById(R.id.back_button); // Initialize back button

        // Set click listeners for navigation buttons on the bottom of the screen
        // button for notification screen
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(UnjoinWaitlistScreen.this, Notifications.class);
            startActivity(newIntent);
        });

        // button for settings screen
        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(UnjoinWaitlistScreen.this, SettingsScreen.class);
            startActivity(newIntent);
        });

        // button for profile screen
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(UnjoinWaitlistScreen.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            startActivity(newIntent);
        });

        // Set click listener for the back button
        /*backButton.setOnClickListener(v -> {
            finish(); // Close the current activity and return to the previous one
        });*/
    }




    /**
     * Retrieves the event's hash data, name, description and poster
     * @param hashedData
     * @param deviceID
     * @param event
     */

    private void retrieveEventInfo(String hashedData, String deviceID, String event) {
        db.collection("entrants")
                .document(deviceID)
                .collection("Waitlisted Events")
                .whereEqualTo("hashed_data", hashedData)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        if (!task.getResult().isEmpty()){  // check something is in result
                            Toast.makeText(UnjoinWaitlistScreen.this, "succesfully read data", Toast.LENGTH_SHORT).show();

                            for (DocumentSnapshot document : task.getResult()) {
                                // Retrieve data directly from the document
                                String eventName = document.getString("eventName");
                                String eventDescription = document.getString("eventDescription");
                                String eventPosterUrl = document.getString("posterUrl");

                                // Call the display method with the retrieved data
                                displayEventInfo(eventName, eventDescription, eventPosterUrl);//eventPosterUrl);
                            }
                        } else {
                            Toast.makeText(UnjoinWaitlistScreen.this, "Event does not exist", Toast.LENGTH_SHORT).show();
                        }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UnjoinWaitlistScreen.this, "Error retrieving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Displays the event name, description and poster
     * @param eventName
     * @param eventDescription
     * @param eventPosterUrl
     */

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

    /**
     * Removes event from entrant's waitlisted events
     * @param userDeviceId
     * @param hashedData
     */

    private void unjoinWaitlistEntrant(String userDeviceId, String hashedData) {
        db.collection("entrants")
                .whereEqualTo("deviceId", userDeviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String entrantId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Access the entrant's "Waitlisted Events" subcollection
                        db.collection("entrants")
                                .document(entrantId)
                                .collection("Waitlisted Events")
                                .whereEqualTo("hashed_data", hashedData)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (!snapshot.isEmpty()) {
                                        // Retrieve the document ID for the event that matches the hashed data
                                        String eventDocId = snapshot.getDocuments().get(0).getId();

                                        // Delete the document representing the waitlisted event
                                        db.collection("entrants")
                                                .document(entrantId)
                                                .collection("Waitlisted Events")
                                                .document(eventDocId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(UnjoinWaitlistScreen.this, "Successfully removed event from waitlist", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Firestore", "Error removing event from waitlist: " + e.getMessage());
                                                    Toast.makeText(UnjoinWaitlistScreen.this, "Error removing from waitlist", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(UnjoinWaitlistScreen.this, "Event not found in waitlist", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(UnjoinWaitlistScreen.this, "Error querying waitlisted events", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(UnjoinWaitlistScreen.this, "Entrant not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UnjoinWaitlistScreen.this, "Error querying entrants", Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes entrant from event's list of entrants
     * @param userDeviceId
     * @param deviceId
     */

    private void unjoinWaitlistOrganizer(String userDeviceId, String deviceId) {
        String event = eventName.getText().toString();

        db.collection("facilities")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String facilityId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("facilities")
                                .document(facilityId)
                                .collection("My Events")
                                .document(event)
                                .collection("waitlist list")
                                .document(userDeviceId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UnjoinWaitlistScreen.this, "Entrant removed from waitlist successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(UnjoinWaitlistScreen.this, "Error removing entrant from waitlist", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(UnjoinWaitlistScreen.this, "Facility not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UnjoinWaitlistScreen.this, "Error querying facility", Toast.LENGTH_SHORT).show());
    }





}
