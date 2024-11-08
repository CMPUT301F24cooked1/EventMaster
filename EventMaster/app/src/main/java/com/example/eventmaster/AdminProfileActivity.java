package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays all of the profiles that have been created
 */
public class AdminProfileActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViewProfilesAdapter viewProfilesAdapter;
    private List<Profile> profileList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private Button deleteButton;
    private boolean isDeleteMode = false;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    //  private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.admin_profile_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        profileList = new ArrayList<>();
        viewProfilesAdapter = new ViewProfilesAdapter(profileList, this, user, true);
        recyclerView.setAdapter(viewProfilesAdapter);

        deleteButton = findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(v -> {
            if (!isDeleteMode) {
                viewProfilesAdapter.toggleCheckBoxVisibility();
                viewProfilesAdapter.notifyDataSetChanged();
                deleteButton.setText("Delete Items");
            } else {
                //delete checked items
                viewProfilesAdapter.deleteSelectedProfiles();
                viewProfilesAdapter.toggleCheckBoxVisibility();
                viewProfilesAdapter.notifyDataSetChanged();
                deleteButton.setText("Select to Delete");
            }
            isDeleteMode = !isDeleteMode; // Toggle mode

        });

        // Retrieve events from Firestore
        retrieveProfiles();

        // Initialize navigation buttons
        ImageButton notificationButton = findViewById(R.id.notifications);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton homeButton = findViewById(R.id.home_icon);
        ImageButton backButton = findViewById(R.id.back_button); // Initialize back button

        // Set result launchers to set up navigation buttons on the bottom of the screen
        settingsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        notificationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        ProfileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {});

        // Set click listeners for navigation buttons on the bottom of the screen
        // sends you to profile screen
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminProfileActivity.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        // sends you to settings screen
        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminProfileActivity.this, SettingsScreen.class);
            newIntent.putExtra("User", user);
            settingsResultLauncher.launch(newIntent);
        });

        // sends you to a list of invited events that you accepted
        homeButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminProfileActivity.this, MainActivity.class);
            newIntent.putExtra("User", user);
            MainActivityResultLauncher.launch(newIntent);
        });
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminProfileActivity.this, Notifications.class);
            newIntent.putExtra("User", user);
            notificationActivityResultLauncher.launch(newIntent);
        });

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            finish(); // Close the current activity and return to the previous one
        });
    }

    private void retrieveProfiles() {
        firestore.collection("profiles").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot profileDoc : task.getResult()) {
                        Profile profile = profileDoc.toObject(Profile.class);
                        profileList.add(profile);
                    }
                    viewProfilesAdapter.notifyDataSetChanged(); // notify the adapter of data changes
                    Log.d("JoinEventScreen", "Number of events: " + profileList.size());
                    Log.d("JoinEventScreen", "Device ID: " + deviceId);

                } else {
                    Log.d("JoinEventScreen", "QuerySnapshot is null");
                }
            }
        });
    }
}
