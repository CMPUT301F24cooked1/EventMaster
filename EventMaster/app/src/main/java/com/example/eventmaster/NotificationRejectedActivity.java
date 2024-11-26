package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * NotificationRejectedActivity handles the display of a notification for being reject from an event.
 * <p>
 * This activity shows the event name and description for a rejected notification.
 * The user can navigate back to the notifications screen.
 * </p>
 */
public class NotificationRejectedActivity extends AppCompatActivity {
    private Profile user;
    private String event_name;
    private String event_detail;
    private String facility_id;

    /**
     * Initializes the activity, sets up views, and retrieves event data from Firestore.
     *
     * @param savedInstanceState Saved instance state for restoring activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(NotificationRejectedActivity.this);
        setContentView(R.layout.notification_rejected_screen);

        user = (Profile) getIntent().getSerializableExtra("User");
        event_name = getIntent().getStringExtra("event_name");
        facility_id = getIntent().getStringExtra("facility_id");

        TextView event_name_text = findViewById(R.id.event_name);

        event_name_text.setText(event_name);
        fetchEventDescription(facility_id, event_name);

        ImageButton backButton = findViewById(R.id.back);

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

    }

    /**
     * Fetches the event description from Firestore and updates the UI.
     *
     * @param facilityId Facility ID associated with the event
     * @param eventName  Name of the event
     */
    private void fetchEventDescription(String facilityId, String eventName) {
        if (facilityId == null || eventName == null) {
            Toast.makeText(this, "Error: Facility ID or Event Name is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Navigate to facilities -> facilityId -> My Events -> eventName
        firestore.collection("facilities")
                .document(facilityId)
                .collection("My Events")
                .document(eventName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve eventDescription field
                        String eventDescription = documentSnapshot.getString("eventDescription");
                        if (eventDescription != null) {
                            updateEventDescriptionView(eventDescription);
                        } else {
                            Toast.makeText(this, "Event description not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch event description", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching event description", e);
                });
    }

    /**
     * Updates the UI with the event description.
     *
     * @param eventDescription The event description to display
     */
    private void updateEventDescriptionView(String eventDescription) {
        TextView eventDescriptionText = findViewById(R.id.event_description);
        eventDescriptionText.setText(eventDescription);
    }

}
