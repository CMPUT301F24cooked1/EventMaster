package com.example.eventmaster;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Notifications extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Connect the layout with the Java class
        setContentView(R.layout.notifications_screen); // Make sure the layout file is named correctly
    }
}
