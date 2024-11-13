package com.example.eventmaster;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The Notifications class represents the activity for displaying notifications within the app.
 * <p>
 * This activity connects to a layout resource file where notifications will be displayed to the user.
 * </p>
 */
public class Notifications extends AppCompatActivity {

    /**
     * Initializes the Notifications activity.
     * <p>
     * This method sets up the activity layout by associating it with the corresponding XML layout file.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after being shut down, this Bundle contains the data it most recently saved.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Connect the layout with the Java class
        setContentView(R.layout.notifications_screen); // Make sure the layout file is named correctly
    }

    // check if user is invited in the firebase

    // if user
}
