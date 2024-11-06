package com.example.eventmaster;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UnjoinWaitlistScreen extends AppCompatActivity {


    //TODO: Update event name and description with corresponding details, update firestore when entrant unjoins waitlist
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waitlist_event_details);
    }
}
