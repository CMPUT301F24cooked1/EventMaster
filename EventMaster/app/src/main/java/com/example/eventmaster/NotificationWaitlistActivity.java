package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * NotificationWaitlistActivity handles the display of a notification for being waitlisted from an event.
 * <p>
 * This activity shows the description for a waitlisted notification.
 * The user can navigate back to the notifications screen.
 * </p>
 */
public class NotificationWaitlistActivity extends AppCompatActivity {
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
        ModeActivity.applyTheme(NotificationWaitlistActivity.this);
        setContentView(R.layout.notification_waitlist_screen);

        user = (Profile) getIntent().getSerializableExtra("User");
        event_name = getIntent().getStringExtra("event_name");
        facility_id = getIntent().getStringExtra("facility_id");

        TextView text = findViewById(R.id.textview);

        text.setText("Unfortunately, you haven not been selected to join  " + event_name+ " from the waitlist at this time.\n\nBut don’t worry! You’re still on the waitlist, and we’ll notify you immediately if you get chosen.");

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
