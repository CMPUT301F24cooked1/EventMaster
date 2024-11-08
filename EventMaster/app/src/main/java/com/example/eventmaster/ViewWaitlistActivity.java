package com.example.eventmaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ViewWaitlistActivity extends AppCompatActivity {

    private RecyclerView waitlistRecyclerView;
    private WaitlistUsersAdapter waitlistAdapter;
    private List<WaitlistUsersAdapter.User> waitlistUsers;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private String eventName; // Define event name or ID

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_waitlist);

        // Retrieve eventName from the Intent
        eventName = getIntent().getStringExtra("eventName");

        waitlistRecyclerView = findViewById(R.id.waitlistRecyclerView);
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        waitlistUsers = new ArrayList<>();

        // Pass the context and eventName to the adapter
        waitlistAdapter = new WaitlistUsersAdapter(waitlistUsers, this, eventName);
        waitlistRecyclerView.setAdapter(waitlistAdapter);

        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Fetch the waitlist from Firebase
        fetchWaitlist();
    }

    private void fetchWaitlist() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("waitlist list")
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

    // Fetch the user's name and profile picture URL based on the device ID
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
                            waitlistUsers.add(user);
                            waitlistAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitlistActivity", "Error fetching user data", e));
    }
}
