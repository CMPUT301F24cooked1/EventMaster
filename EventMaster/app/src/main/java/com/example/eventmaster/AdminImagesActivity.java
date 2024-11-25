package com.example.eventmaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import io.grpc.Context;

/**
 * Displays all of the images that have been created
 */
public class AdminImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ViewImagesAdapter viewImagesAdapter;
    private List<String> imageList;
    private FirebaseStorage storage;
    private String deviceId; // Replace with actual device ID
    private Button deleteButton;
    private boolean isDeleteMode = false;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.admin_images_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity
        deviceId = user.getDeviceId();

        // Initialize Firebase Firestore
        storage = FirebaseStorage.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageList = new ArrayList<String>();

        viewImagesAdapter = new ViewImagesAdapter(imageList, this, user, true);
        recyclerView.setAdapter(viewImagesAdapter);

        deleteButton = findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(v -> {
            if (!isDeleteMode) {
                viewImagesAdapter.toggleCheckBoxVisibility();
                viewImagesAdapter.notifyDataSetChanged();
                deleteButton.setText("Delete Items");
            } else {
                //delete checked items
                viewImagesAdapter.deleteSelectedImages();
                viewImagesAdapter.toggleCheckBoxVisibility();
                viewImagesAdapter.notifyDataSetChanged();
                deleteButton.setText("Select to Delete");
            }
            isDeleteMode = !isDeleteMode; // Toggle mode

        });

        // Retrieve events from Firestore
        retrieveImages();


        // Initialize navigation buttons
        ImageButton notificationButton = findViewById(R.id.notifications);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton homeButton = findViewById(R.id.home_icon);
        ImageButton backButton = findViewById(R.id.back_button); // Initialize back button

        // Set result launchers to set up navigation buttons on the bottom of the screen
        settingsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                });

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                });

        notificationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                });

        ProfileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                });

        // Set click listeners for navigation buttons on the bottom of the screen
        // sends you to profile screen
        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminImagesActivity.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        // sends you to settings screen
        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminImagesActivity.this, SettingsScreen.class);
            newIntent.putExtra("User", user);
            settingsResultLauncher.launch(newIntent);
        });

        // sends you to a list of invited events that you accepted
        homeButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminImagesActivity.this, MainActivity.class);
            newIntent.putExtra("User", user);
            MainActivityResultLauncher.launch(newIntent);
        });
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(AdminImagesActivity.this, Notifications.class);
            newIntent.putExtra("User", user);
            notificationActivityResultLauncher.launch(newIntent);
        });

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            finish(); // Close the current activity and return to the previous one
        });
    }


    /**
     * Gets the string url of the images, and saves them into imageList, to transfer to ViewImagesAdapter
     */
    private void retrieveImages() {
        StorageReference storageRef = storage.getReference();
        StorageReference folderRef = storageRef.child("event_posters");

        folderRef.listAll().addOnSuccessListener(listResult -> {
            List<Task<Uri>> tasks = new ArrayList<>();

            // get all image urls
            for (StorageReference fileRef : listResult.getItems()) {
                tasks.add(fileRef.getDownloadUrl());
            }

            // firebase storage retrieval is async so need a listener when everything is done
            Tasks.whenAllSuccess(tasks).addOnSuccessListener((List<Object> results) -> {
                // adds everything to imageList
                for (Object result : results) {
                    if (result instanceof Uri) {
                        imageList.add(((Uri) result).toString());
                    }
                }
                viewImagesAdapter.notifyDataSetChanged();
                Log.d("FirebaseStorage", "All image URLs retrieved and adapter updated.");
            }).addOnFailureListener(e -> {
                Log.e("FirebaseStorage", "Failed to retrieve all URLs.", e);
            });
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error listing images", e);
        });
    }
}

