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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

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
    AppCompatButton joinWaitlistButton;
    ActivityResultLauncher<Intent> joinWaitlistResultLauncher;
    private Profile user;
    private String name;
    private String email;
    private String phone_number;


    /**
     * Creates the event information screen where the user can view event details, name and poster
     * There is also a button that allows them to join the waiting list for the event, this will update the firestore database
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.event_details_screen);
        eventName = findViewById(R.id.event_name);
        eventDescription = findViewById(R.id.event_decription);
        eventFinalDate = findViewById(R.id.event_decription);
        eventPoster = findViewById(R.id.event_poster);
        joinWaitlistButton = findViewById(R.id.join_waitlist_button);

        db = FirebaseFirestore.getInstance();

        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("User");
        // will need to access user device id but just hardcoded for now
        String userDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        //retrieves the data for the profile
        db.collection("profiles").document(userDeviceId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            name = documentSnapshot.getString("name");
                            email = documentSnapshot.getString("email");
                            phone_number = documentSnapshot.getString("phone number");
                        }else{
                            Log.d("Firestore", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error getting profile information", e);
                    }
                });


        // retrieve information after scanning
        Intent intent = getIntent();
        String hashedData = intent.getStringExtra("hashed_data");
        String deviceID = intent.getStringExtra("deviceID");  // facility id
        String event = intent.getStringExtra("event");
        String posterUrl = intent.getStringExtra("posterUrl");


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


        // links the description screen to the join waitlist screen
        joinWaitlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(retrieveEventInfo.this, JoinWaitlistScreen.class);
                intent.putExtra("User", user);
                fetchEventData(hashedData, deviceID, event, posterUrl);
                joinWaitlistEntrant(userDeviceId, hashedData, deviceID, posterUrl);
                joinWaitlistOrganizer(userDeviceId, deviceID);
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

    /**
     * Retrieves the event data(name, description, poster)
     * @param hashedData
     * @param deviceID
     * @param event
     */

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
     * Sends data to JoinWaitlist screen
     * @param hashedData
     * @param deviceID
     * @param event
     * @param posterUrl
     */

    private void fetchEventData(String hashedData, String deviceID, String event, String posterUrl) {
        Intent intent2 = new Intent(retrieveEventInfo.this, JoinWaitlistScreen.class);
        intent2.putExtra("hashed_data", hashedData);
        intent2.putExtra("deviceID", deviceID);
        intent2.putExtra("event", event);
        intent2.putExtra("posterUrl", posterUrl);

        startActivity(intent2);

    }

    /**
     * Adds a collection to firebase of the events that are the entrant waitlisted
     * @param userDeviceId entrant's user id
     */

    private void joinWaitlistEntrant(String userDeviceId, String hashedData, String deviceID, String posterUrl) {
        String event = eventName.getText().toString();
        Map<String, Object> WaitlistEvents = new HashMap<>();
        WaitlistEvents.put("eventName", event);
        WaitlistEvents.put("hashed_data", hashedData);
        WaitlistEvents.put("deviceID", deviceID);
        WaitlistEvents.put("posterUrl", posterUrl);

        // add event to the entrants list of waitlisted events
        db.collection("entrants")
                .whereEqualTo("deviceId", userDeviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String entrantId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("entrants")
                                .document(entrantId)
                                .collection("Waitlisted Events")
                                .document(event)
                                .set(WaitlistEvents)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(retrieveEventInfo.this, "Successfully joined waitlist", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(retrieveEventInfo.this, "Error joining waitlist", Toast.LENGTH_SHORT).show());
                    }else{
                        Toast.makeText(retrieveEventInfo.this, "Waitlist not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(retrieveEventInfo.this, "Error querying entrants", Toast.LENGTH_SHORT).show());
    }


    /**
     * Adds user id to a collection of users waitlisted in Firebase
     * @param userDeviceId the user joining the waitlist
     * @param deviceId the facility id correlated to the event
     */
    private void joinWaitlistOrganizer(String userDeviceId, String deviceId) {
        String event = eventName.getText().toString();

        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("entrantId", userDeviceId);
        entrantData.put("email", email);
        entrantData.put("name", name);
        entrantData.put("phone number", phone_number);

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
                                .set(entrantData)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(retrieveEventInfo.this, "Entrant added to waitlist successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(retrieveEventInfo.this, "Error adding entrant to waitlist", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(retrieveEventInfo.this, "Entrant not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(retrieveEventInfo.this, "Error querying entrant", Toast.LENGTH_SHORT).show());
    }


}

