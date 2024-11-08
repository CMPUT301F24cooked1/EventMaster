package com.example.eventmaster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class OrganizerEventListView extends AppCompatActivity{


    private TextView eventNameTextView, eventDescriptionTextView;
    private ImageView  eventPosterImageView;
    private ImageButton editPosterButton;
    private String eventId;
    private static Bitmap cachedPosterBitmap; // Static variable to cache the poster image
    private String deviceId; // Replace with actual device ID
    private Profile user;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    private Uri updatedPosterUri; // To hold the URI of the selected poster
    private String posterDownloadUrl = null; // To hold the download URL of the uploaded poster

    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private Profile user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.organizer_event_list_view_screen);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("User");
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        editPosterButton = findViewById(R.id.editPoster);
        eventPosterImageView = findViewById(R.id.eventPosterImageView); // Reference the poster ImageView

        // Retrieve the event ID from the intent
        eventId = getIntent().getStringExtra("eventId");
        Log.d("eventID: ", "event ID: " + eventId);

        // Load event details from Firestore
        loadEventDetails();
        // Initialize navigation buttons
        ImageButton notificationButton = findViewById(R.id.notifications);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton homeButton = findViewById(R.id.home_icon);
        ImageButton backButton = findViewById(R.id.back_button); // Initialize back button

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        user = user = (Profile) getIntent().getSerializableExtra("User");


        AppCompatButton waitingListButton = findViewById(R.id.waiting_list_button);

        editPosterButton.setOnClickListener(v -> openFileChooser());


        // Set click listeners for navigation
        notificationButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(OrganizerEventListView.this, Notifications.class);
            newIntent.putExtra("User", user);
            notificationActivityResultLauncher.launch(newIntent);
        });


        settingsButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(OrganizerEventListView.this, SettingsScreen.class);
            newIntent.putExtra("User", user);
            settingsResultLauncher.launch(newIntent);
        });

        profileButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(OrganizerEventListView.this, ProfileActivity.class);
            newIntent.putExtra("User", user);
            ProfileActivityResultLauncher.launch(newIntent);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventListView.this, MainActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        settingsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        notificationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        ProfileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        waitingListButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventListView.this, ViewWaitlistActivity.class);
            intent.putExtra("eventName", eventNameTextView.getText().toString()); // Pass the event name or ID as an extra
            startActivity(intent);
        });

    }

    /**
     * Open the file chooser to select an image for the poster
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handle the result of image selection.
     * Saves newly selected poster uri and calls updatePoster()
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            updatedPosterUri = data.getData(); // Store the URI of the selected poster
            Toast.makeText(this, "New poster selected.", Toast.LENGTH_SHORT).show();
            Log.d("posterUri: ", "posterUri: " + updatedPosterUri);
            updatePoster(); // Update poster in all places
        }
    }

    private void loadEventDetails() {
        firestore = FirebaseFirestore.getInstance();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        DocumentReference eventRef = firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventId); // Use the event ID to get the specific document

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventName = documentSnapshot.getString("eventName");
                String eventDescription = documentSnapshot.getString("eventDescription");

                eventNameTextView.setText(eventName);
                eventDescriptionTextView.setText(eventDescription);

                // Call the loadEventPoster method to load the poster image
                if (eventName != null) {
                    loadEventPoster(eventName);
                }
            }
        });
    }

    /**
     * loads the Event poster into the app if it is cached. Calls loadNewPoster otherwise.
     * @param eventName the event name corresponding to the poster
     */
    private void loadEventPoster(String eventName) {
        if (cachedPosterBitmap != null) {
            // If the poster is already cached, set it directly
            eventPosterImageView.setImageBitmap(cachedPosterBitmap);
        } else {
            loadNewPoster(eventName);
        }
    }

    /**
     * Loads the poster from firebase according to the given event name
     * @param eventName the event name corresponding to the poster
     */
    private void loadNewPoster(String eventName) {
        storage = FirebaseStorage.getInstance();
        StorageReference posterRef = storage.getReference().child("event_posters/" + eventName + "_poster");

        // Get the download URL
        posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Use Glide to load the image from the download URL
            Glide.with(this)
                    .asBitmap() // Load as Bitmap to cache it
                    .load(uri)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            cachedPosterBitmap = resource; // Cache the loaded image
                            eventPosterImageView.setImageBitmap(cachedPosterBitmap); // Set the image
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            eventPosterImageView.setImageDrawable(errorDrawable);
                            // Optionally set the default image here as well
                            eventPosterImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_poster));
                        }
                    });
        }).addOnFailureListener(e -> {
            // Handle the error here, maybe show a toast
            //Toast.makeText(this, "Failed to load poster: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            eventPosterImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_poster)); // Show default poster on failure
        });
    }

    /**
     * Updates the storage reference for the poster when a new one is chosen.
     * Saves URL to posterDownloadUrl
     */
    private void updatePoster() {
        storageRef = storage.getReference();
        if (updatedPosterUri != null) {
            StorageReference posterRef = storageRef.child("event_posters/" + eventId + "_poster");

            posterRef.putFile(updatedPosterUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            posterDownloadUrl = uri.toString(); // Save the download URL
                            updatePosterFirestore(); // Once the poster is uploaded, update it in Firestore
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(OrganizerEventListView.this, "Poster upload failed", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Updates the poster URL in Firestore
     * Calls loadNewPoster on the event to reload with new poster.
     */
    private void updatePosterFirestore() {
        Map<String, Object> posterData = new HashMap<>();

        // Include the poster URL if it exists
        if (posterDownloadUrl != null) {
            posterData.put("posterUrl", posterDownloadUrl);
            firestore.collection("facilities")
                    .document(deviceId).collection("My Events").document(eventId)
                    .set(posterData, SetOptions.merge()) // Merge with existing data
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Poster info updated successfully.");
                        loadNewPoster(eventId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error updating Poster info", e);
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cachedPosterBitmap = null; // Clear the cached bitmap when the activity is destroyed
    }
}