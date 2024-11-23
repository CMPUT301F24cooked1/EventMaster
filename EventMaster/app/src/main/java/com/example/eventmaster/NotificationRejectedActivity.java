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

public class NotificationRejectedActivity extends AppCompatActivity {

    private Profile user;
    private String event_name;
    private String event_detail;
    private String facility_id;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> listResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(NotificationRejectedActivity.this);
        setContentView(R.layout.notification_invited_screen);

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

    private void updateEventDescriptionView(String eventDescription) {
        TextView eventDescriptionText = findViewById(R.id.event_description);
        eventDescriptionText.setText(eventDescription);
    }


}
