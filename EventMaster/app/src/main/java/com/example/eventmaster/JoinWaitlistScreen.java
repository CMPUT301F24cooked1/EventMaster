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

import java.util.HashMap;
import java.util.Map;

public class JoinWaitlistScreen extends AppCompatActivity {
    private FirebaseFirestore db;
    TextView eventName;
    TextView eventDescription;
    TextView eventFinalDate;
    ImageView eventPoster;
    ImageButton notificationButton;
    ImageButton settingsButton;
    ImageButton profileButton;
    ImageButton listButton;
    private Profile user;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;

    /**
     * Initializes screen telling user they have joined the waitlist
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.join_waitlist_screen);
        eventName = findViewById(R.id.event_name);
        eventDescription = findViewById(R.id.event_decription);
        eventFinalDate = findViewById(R.id.event_decription);
        eventPoster = findViewById(R.id.event_poster);
        ImageButton backButton = findViewById(R.id.back_button);

        db = FirebaseFirestore.getInstance();

        user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

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

        settingsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        notificationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        ProfileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });


    }

    /**
     * Retrieves the event's hash data, name, description and poster
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
                            Toast.makeText(JoinWaitlistScreen.this, "succesfully read data", Toast.LENGTH_SHORT).show();

                            for (DocumentSnapshot document : task.getResult()) {
                                // Retrieve data directly from the document
                                String eventName = document.getString("eventName");
                                String eventDescription = document.getString("eventDescription");
                                String eventPosterUrl = document.getString("posterUrl");
                                String waitlistCountdown = document.getString("waitlist_countdown");

                                // Call the display method with the retrieved data
                                displayEventInfo(eventName, eventDescription, eventPosterUrl, waitlistCountdown);//eventPosterUrl);
                            }
                        } else {
                            Toast.makeText(JoinWaitlistScreen.this, "Event does not exist", Toast.LENGTH_SHORT).show();
                        }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JoinWaitlistScreen.this, "Error retrieving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays the event name, description and poster
     * @param eventName
     * @param eventDescription
     * @param eventPosterUrl
     */

    private void displayEventInfo(String eventName, String eventDescription, String eventPosterUrl, String waitlistCountdown) {
        TextView eventNameTextView = findViewById(R.id.event_name);
        TextView eventDescriptionTextView = findViewById(R.id.event_decription);
        TextView countdownTextView = findViewById(R.id.event_open_time);

        // Set the text for the TextViews
        eventNameTextView.setText(eventName);
        eventDescriptionTextView.setText(eventDescription);
        countdownTextView.setText(waitlistCountdown);

        if (eventPosterUrl == null){
            eventPoster.setImageResource(R.drawable.default_poster);  // set default poster
        }
        else {
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



}

