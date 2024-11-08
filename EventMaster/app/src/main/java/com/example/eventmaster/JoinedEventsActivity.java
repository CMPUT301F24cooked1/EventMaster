package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class JoinedEventsActivity extends AppCompatActivity {
    /**
     * Initializes the Joined Events Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view all joined events
     */
    private RecyclerView recyclerView;
    // private EventAdapter eventAdapter;
    private ViewEventsAdapter ViewEventsAdapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private FirebaseFirestore db; // Firestore instance
    private ActivityResultLauncher<Intent> waitlistedEventsActivityResultLauncher;

    //  private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joined_events_screen);
        ModeActivity.applyTheme(this);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity



        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        ViewEventsAdapter = new ViewEventsAdapter(eventList, this, user, false);
        recyclerView.setAdapter(ViewEventsAdapter);
        // Retrieve events from Firestore
        retrieveEvents();

        // Connecting joined events screen to waitlisted events screen
        waitlistedEventsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        //links joined events screen to the waitlisted events screen
        AppCompatButton viewWaitlist = findViewById(R.id.view_waitlist_button);
        viewWaitlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinedEventsActivity.this, WaitlistedEventsActivity.class);
                intent.putExtra("User", user);
                waitlistedEventsActivityResultLauncher.launch(intent);
            }
        });
    }

    private void retrieveEvents() {
        //TODO: Retrieve the events that the user has joined from firestore
    }
}