package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * NotificationAttendeesActivity handles the display of a notification for being an attendee for an event.
 * <p>
 * This activity shows the description for an attendee notification.
 * The user can navigate back to the notifications screen.
 * </p>
 */
public class NotificationAttendeesActivity extends AppCompatActivity {
    private Profile user;
    private String event_name;
    private String facility_id;

    /**
     * Initializes the activity and sets up views
     *
     * @param savedInstanceState Saved instance state for restoring activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(NotificationAttendeesActivity.this);
        setContentView(R.layout.notification_waitlist_screen);

        user = (Profile) getIntent().getSerializableExtra("User");
        event_name = getIntent().getStringExtra("event_name");
        facility_id = getIntent().getStringExtra("facility_id");

        TextView text = findViewById(R.id.textview);

        text.setText("Congratulations! You've chosen to attend the " + event_name + " event. Check your Joined Events List and click on the " + event_name + " event for all the details!");

        ImageButton backButton = findViewById(R.id.back);

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

    }
}
