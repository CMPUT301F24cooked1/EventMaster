package com.example.eventmaster;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewInvitedListActivity extends AppCompatActivity {

    private RecyclerView invitedRecyclerView;
    private WaitlistUsersAdapter invitedAdapter;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private String eventName; // Define event name or ID
    private List<WaitlistUsersAdapter.User> invitedUsers;

    /**
     * Displays the list of users who have been invited to come to an Event.
     * @param savedInstanceState If the activity is restarted this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_invited_list);

        eventName = getIntent().getStringExtra("myEventName");

        invitedUsers = new ArrayList<>();

        invitedRecyclerView = findViewById(R.id.waitlistRecyclerView);
        invitedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass the context and eventName to the adapter
        invitedAdapter = new WaitlistUsersAdapter(invitedUsers, this, eventName);
        invitedRecyclerView.setAdapter(invitedAdapter);

        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Fetch the waitlist from Firebase
        fetchInvitedList();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Gets the invited list from a specific event and calls fetchUserName on each device ID.
     */
    private void fetchInvitedList() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("invited list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userDeviceId = document.getId();
                            fetchUserName(userDeviceId); // Fetch the name for each userDeviceId
                        }
                    } else {
                        Log.e("WaitlistActivity", "Error getting waitlist: ", task.getException());
                    }
                });
    }

    //
    /**
     * Fetches the user's name and profile picture URL based on the device ID. Saves them to invitedUsers
     * @param userDeviceId The device ID to fetch the username and profile picture from.
     */
    private void fetchUserName(String userDeviceId) {
        firestore.collection("profiles")
                .document(userDeviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name"); // Assuming 'name' is the field for the user's name
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl"); // Assuming 'profilePictureUrl' is the field for profile picture

                        if (userName != null) {
                            // Create a User object and add it to the waitlist
                            WaitlistUsersAdapter.User user = new WaitlistUsersAdapter.User(userName, profilePictureUrl);
                            invitedUsers.add(user);
                            invitedAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitlistActivity", "Error fetching user data", e));
    }
}