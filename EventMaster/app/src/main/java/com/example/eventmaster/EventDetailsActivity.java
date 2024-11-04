package com.example.eventmaster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EventDetailsActivity extends AppCompatActivity {
    private TextView eventNameTextView, eventDescriptionTextView;
    private ImageView qrCodeImageView, eventPosterImageView;
    private String eventId;
    private static Bitmap cachedPosterBitmap; // Static variable to cache the poster image
    private String deviceId; // Replace with actual device ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        eventPosterImageView = findViewById(R.id.eventPosterImageView); // Reference the poster ImageView

        // Retrieve the event ID from the intent
        eventId = getIntent().getStringExtra("eventId");

        // Load event details from Firestore
        loadEventDetails();
        // Initialize navigation buttons
        ImageButton notificationButton = findViewById(R.id.notifications);
        ImageButton settingsButton = findViewById(R.id.settings);
        ImageButton profileButton = findViewById(R.id.profile);
        ImageButton homeButton = findViewById(R.id.home_icon);
        ImageButton backButton = findViewById(R.id.back_button); // Initialize back button
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Profile user = new Profile(deviceId,"Vansh", " ", " ");

        // Set click listeners for navigation
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, Notifications.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, SettingsScreen.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, ProfileActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, MainActivity.class);
            startActivity(intent);
        });
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            finish(); // Close the current activity and return to the previous one
        });

    }


    private void loadEventDetails() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        DocumentReference eventRef = firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(); // Use the event ID to get the specific document

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventName = documentSnapshot.getString("eventName");
                String eventDescription = documentSnapshot.getString("eventDescription");
                String qrCodeHash = documentSnapshot.getString("hash"); // Assuming you store this

                eventNameTextView.setText(eventName);
                eventDescriptionTextView.setText(eventDescription);

                // Load QR code using the hash
                if (qrCodeHash != null) {
                    loadQRCode(qrCodeHash);
                } else {
                    Toast.makeText(this, "QR code hash is missing.", Toast.LENGTH_SHORT).show();
                }

                // Call the loadEventPoster method to load the poster image
                if (eventName != null) {
                    loadEventPoster(eventName);
                }
            }
        });
    }

    private void loadQRCode(String qrCodeHash) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeHash, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEventPoster(String eventName) {
        if (cachedPosterBitmap != null) {
            // If the poster is already cached, set it directly
            eventPosterImageView.setImageBitmap(cachedPosterBitmap);
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cachedPosterBitmap = null; // Clear the cached bitmap when the activity is destroyed
    }
}
