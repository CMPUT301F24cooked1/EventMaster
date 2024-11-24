package com.example.eventmaster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Activity to display detailed information about a specific event, including event name,
 * description, QR code, and event poster image.
 * This activity retrieves event data from Firestore and displays it.
 */
public class EventDetailsActivity extends AppCompatActivity {
    private TextView eventNameTextView, eventDescriptionTextView;
    private ImageView qrCodeImageView, eventPosterImageView;
    private String eventId;
    private static Bitmap cachedPosterBitmap; // Static variable to cache the poster image
    private String deviceId; // Replace with actual device ID
    private Profile user;
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private android.content.Intent Intent;


    /**
            * Called when the activity is created. Initializes the UI components and retrieves
     * event details from Firestore.
            *
            * @param savedInstanceState If the activity is being reinitialized, this is the previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.activity_event_details);

        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        eventPosterImageView = findViewById(R.id.eventPosterImageView); // Reference the poster ImageView


        // Retrieve the event ID from the intent
        eventId = getIntent().getStringExtra("eventId");
        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("user");

        // Load event details from Firestore
        loadEventDetails();
        ImageButton backButton = findViewById(R.id.back);
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Set result launchers to set up navigation buttons on the bottom of the screen
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


        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Disable tint for specific menu item
        Menu menu = bottomNavigationView.getMenu();
        MenuItem qrCodeItem = menu.findItem(R.id.nav_scan_qr);
        Drawable qrIcon = qrCodeItem.getIcon();
        qrIcon.setTintList(null);  // Disable tinting for this specific item
        // Set up navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Intent newIntent;

            if (item.getItemId() == R.id.nav_Home) {
                newIntent = new Intent(EventDetailsActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(EventDetailsActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(EventDetailsActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(EventDetailsActivity.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            }else if (item.getItemId() == R.id.nav_scan_qr) {
                openQRScanFragment();
                return true;
            }
            return false;
        });



    }
    private void openQRScanFragment() {
        // Open QRScanFragment without simulating button click
        Intent intent = new Intent(this, QRScanFragment.class);
        intent.putExtra("User", user);  // Pass the user information if needed
        startActivity(intent);

    }



    /**
     * Loads event details from Firestore, including event name, description, and QR code.
     */
    private void loadEventDetails() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        DocumentReference eventRef = firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventId); // Use the event ID to get the specific document

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventName = documentSnapshot.getString("eventName");
                String eventDescription = documentSnapshot.getString("eventDescription");
                String qrCodeHash = documentSnapshot.getString("hash");

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
    /**
     * Generates and displays a QR code based on the provided hash.
     *
     * @param qrCodeHash The hash value used to generate the QR code.
     */
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
    /**
     * Loads the event poster image from Firebase Storage.
     * If the poster is already cached, it uses the cached version.
     *
     * @param eventName The name of the event used to locate the poster image.
     */

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

    /**
     * Clears the cached poster image when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cachedPosterBitmap = null; // Clear the cached bitmap when the activity is destroyed
    }
}
