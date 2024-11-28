package com.example.eventmaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.TextView;
import java.util.Calendar;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import android.widget.AutoCompleteTextView;

/**
 * CreateEventActivity.java
 * This activity allows users to create an event by filling in details such as event name, description, capacity, waitlist options,
 * and a countdown timer. Users can also upload a poster and enable geolocation for the event.
 * The event is saved to Firebase Firestore and Firebase Storage, and a unique QR code is generated for each event.
 * Users are navigated to the event details screen upon successful event creation.
 */
public class CreateEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String deviceId;
    // UI Elements
    private EditText eventNameInput;
    private EditText eventDescriptionInput;
    private EditText eventCapacityInput;
    private EditText waitlistCapacityInput;
    private EditText waitlistCountdownInput;
    private AppCompatButton createEventButton;
    private TextView uploadPosterButton;
    private Switch geolocationSwitch;
    private Profile user;
    private Uri posterUri; // To hold the URI of the selected poster
    private String posterDownloadUrl = null; // To hold the download URL of the uploaded poster
    private AutoCompleteTextView eventVenue;
    private String eventVenueAddress;

    private TextView waitlistCountdownText;
    private Calendar waitlistCalendar;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image selection
    /**
     * Initializes the activity, including UI elements and Firebase references.
     * Sets up button listeners for creating events, uploading posters, and navigating to other activities.
     * @param savedInstanceState State of the app if being restored after being closed.
     */
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();
        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("User");
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check if user was grabbed
        assert user != null;

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize UI elements
        eventNameInput = findViewById(R.id.eventName);
        eventDescriptionInput = findViewById(R.id.eventDescription);
        eventCapacityInput = findViewById(R.id.eventCapacity);
        waitlistCapacityInput = findViewById(R.id.waitlistCapacity);
        waitlistCountdownInput = findViewById(R.id.waitlistCountdown);
        createEventButton = findViewById(R.id.createEventButton);
        uploadPosterButton = findViewById(R.id.Upload_poster_button);

        ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
        ActivityResultLauncher<Intent> notificationActivityResultLauncher;
        ActivityResultLauncher<Intent> settingsResultLauncher;
        ActivityResultLauncher<Intent> MainActivityResultLauncher;
        android.content.Intent Intent;

        geolocationSwitch = findViewById(R.id.geolocation_switch);

        waitlistCountdownText = findViewById(R.id.waitlistCountdown);
        waitlistCalendar = Calendar.getInstance();

        // Open date picker on TextView click
        waitlistCountdownText.setOnClickListener(v -> showDateTimePicker());

        ImageButton backButton = findViewById(R.id.back);
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });


        // Set up the upload poster button
        uploadPosterButton.setOnClickListener(v -> openFileChooser()); // Open the image chooser

        // Set up the create event button
        createEventButton.setOnClickListener(v -> {
            // Call createEvent before proceeding to the next activity
            createEvent();
        });

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
                newIntent = new Intent(CreateEventActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(CreateEventActivity.this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(CreateEventActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(CreateEventActivity.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            }else if (item.getItemId() == R.id.nav_scan_qr) {
                Intent intent = new Intent(CreateEventActivity.this, QRScannerActivity.class);
                startActivity(intent);
                return true;

            }
            return false;
        });
        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        eventVenue = findViewById(R.id.eventVenue);

        // Set up the autocomplete feature for venue input
        eventVenue.setOnClickListener(v -> openPlaceAutocomplete());


    }

    private void openPlaceAutocomplete() {
        // Create an intent for Place Autocomplete
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
                .build(this);

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    // Handle the result from the autocomplete activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Handle the autocomplete result
                Place place = Autocomplete.getPlaceFromIntent(data);
                eventVenueAddress = place.getAddress();
                eventVenue.setText(eventVenueAddress);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle autocomplete error
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }  if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            posterUri = data.getData(); // Get the selected image URI
            if (posterUri != null) {
                Log.d("onActivityResult", "Poster URI set: " + posterUri.toString());
                Toast.makeText(this, "Poster selected", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("onActivityResult", "Poster URI is null");
            }
        }
    }




    private void openQRScanFragment() {
        // Open QRScanFragment without simulating button click
        Intent intent = new Intent(this, QRScanFragment.class);
        intent.putExtra("User", user);  // Pass the user information if needed
        startActivity(intent);

    }



    /**
     * Displays a date and time picker for the waitlist countdown. Ensures the selected date is in the future.
     */
    private void showDateTimePicker() {
        // Open the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            waitlistCalendar.set(Calendar.YEAR, year);
            waitlistCalendar.set(Calendar.MONTH, month);
            waitlistCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Open the TimePickerDialog after selecting date
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                waitlistCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                waitlistCalendar.set(Calendar.MINUTE, minute);

                // Format date and time to display in TextView
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedDate = dateFormat.format(waitlistCalendar.getTime());
                waitlistCountdownText.setText(formattedDate);

            }, waitlistCalendar.get(Calendar.HOUR_OF_DAY), waitlistCalendar.get(Calendar.MINUTE), true).show();

        }, waitlistCalendar.get(Calendar.YEAR), waitlistCalendar.get(Calendar.MONTH), waitlistCalendar.get(Calendar.DAY_OF_MONTH));

        // Disable all past dates by setting the minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    /**
     * Opens a file chooser to select an image for the event poster.
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    /**
     * Validates event inputs and initiates the creation of an event in Firebase.
     * If a poster is selected, it is uploaded first, and the event is saved afterwards.
     */
    private void createEvent() {
        String eventName = eventNameInput.getText().toString();
        String eventDescription = eventDescriptionInput.getText().toString();
        String eventCapacity = eventCapacityInput.getText().toString();
        String waitlistCapacity = waitlistCapacityInput.getText().toString();
        String waitlistCountdown = waitlistCountdownText.getText().toString();

        // Input validation
        if (eventName.isEmpty() || eventDescription.isEmpty() || eventCapacity.isEmpty() || waitlistCountdown.isEmpty()|| eventVenueAddress.isEmpty())  {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the event name is unique
        db.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .whereEqualTo("eventName", eventName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(CreateEventActivity.this, "Event name must be unique", Toast.LENGTH_SHORT).show();
                    } else {
                        // Validate event capacity
                        int capacity;
                        try {
                            capacity = Integer.parseInt(eventCapacity);
                            if (capacity <= 0) {
                                Toast.makeText(this, "Event capacity must be a positive integer", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Event capacity must be a number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Validate waitlist capacity (if provided)
                        if (!waitlistCapacity.isEmpty()) {
                            int waitlistCap;
                            try {
                                waitlistCap = Integer.parseInt(waitlistCapacity);
                                if (waitlistCap < 0) {
                                    Toast.makeText(this, "Waitlist capacity cannot be negative", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(this, "Waitlist capacity must be a number", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        // Date format validation for waitlistCountdown
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        dateFormat.setLenient(false);
                        try {
                            // Validate format and check if the date is in the past
                            if (dateFormat.parse(waitlistCountdown).getTime() <= System.currentTimeMillis()) {
                                Toast.makeText(this, "Waitlist countdown must be a future date", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (ParseException e) {
                            Toast.makeText(this, "Invalid date format for waitlistCountdown. Use yyyy-MM-dd HH:mm:ss.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // First, upload the poster (if one is selected)
                        if (posterUri != null) {
                            StorageReference posterRef = storageRef.child("event_posters/" + eventName + "_poster");

                            posterRef.putFile(posterUri)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        Log.d("PosterUpload", "Upload Success");
                                        posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                            Log.d("PosterDownloadUrl", "Poster URL: " + uri.toString());
                                            posterDownloadUrl = uri.toString();
                                            // Continue saving event after poster upload
                                            saveEventToFirestore(eventName, eventDescription, eventCapacity, waitlistCapacity, waitlistCountdown, eventVenueAddress);
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("PosterUpload", "Upload Failed", e);
                                        Toast.makeText(CreateEventActivity.this, "Poster upload failed", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // No poster selected, just save the event
                            saveEventToFirestore(eventName, eventDescription, eventCapacity, waitlistCapacity, waitlistCountdown,eventVenueAddress);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Error checking event name uniqueness", Toast.LENGTH_SHORT).show());
    }

    /**
     * Saves event details to Firestore, including poster URL and geolocation information if provided.
     * @param eventName The name of the event.
     * @param eventDescription Description of the event.
     * @param eventCapacity The maximum capacity for the event.
     * @param waitlistCapacity Capacity for the waitlist.
     * @param waitlistCountdown Date and time for the waitlist countdown.
     */
    private void saveEventToFirestore(String eventName, String eventDescription, String eventCapacity, String waitlistCapacity, String waitlistCountdown, String eventVenueAddress) {
        // Create a map for event data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", eventName);
        eventData.put("eventDescription", eventDescription);
        eventData.put("eventCapacity", Integer.parseInt(eventCapacity));
        eventData.put("waitlistCapacity", waitlistCapacity.isEmpty() ? 0 : Integer.parseInt(waitlistCapacity));
        eventData.put("waitlistCountdown", waitlistCountdown);
        eventData.put("eventVenue", eventVenueAddress);
        // Get the state of the geolocation switch
        boolean isGeolocationEnabled = geolocationSwitch.isChecked();
        eventData.put("geolocationEnabled", isGeolocationEnabled); // Add geolocation status to event data

        // Include the poster URL if it exists
        if (posterDownloadUrl != null) {
            eventData.put("posterUrl", posterDownloadUrl);
            Log.d("Firestore", "Poster URL added: " + posterDownloadUrl);
        } else {
            Log.d("Firestore", "No poster URL, skipping");
        }

        // Query the "facilities" collection for the document with the matching deviceId
        db.collection("facilities")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String facilityId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("facilities")
                                .document(facilityId)
                                .collection("My Events")
                                .document(eventName)
                                .set(eventData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                                    // Generate and store QR code
                                    generateQRCode(eventName, facilityId);
                                    // Navigate directly to EventDetailsActivity
                                    Intent intent = new Intent(CreateEventActivity.this, EventDetailsActivity.class);
                                    intent.putExtra("eventId", eventName); // Pass the event ID or name as needed
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Error creating event", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(CreateEventActivity.this, "Facility not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Error querying facilities", Toast.LENGTH_SHORT).show());
    }

    /**
     * Generates a QR code for the event's link and stores its hash in Firestore.
     * @param eventName The name of the event for generating the QR code.
     * @param facilityId The facility ID associated with the event.
     */
    private void generateQRCode(String eventName, String facilityId) {
        // Link to the event's details (description and poster URL)
        String eventLink = "https://yourapp.com/event/" + facilityId + "/" + eventName;

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(eventLink, BarcodeFormat.QR_CODE, 400, 400);

            // Store the hash of the event URL
            String hash = generateHash(eventLink);
            storeHashInFirestore(eventName, facilityId, hash);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a hash for the input string using SHA-256.
     * @param input The input string to hash.
     * @return The hashed string in hexadecimal format.
     */
    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores the hash of the event's QR code in Firestore.
     * @param eventName The name of the event for which the QR code hash is stored.
     * @param facilityId The facility ID associated with the event.
     * @param hash The hash string to store.
     */
    private void storeHashInFirestore(String eventName, String facilityId, String hash) {
        Map<String, Object> qrCodeData = new HashMap<>();
        qrCodeData.put("hash", hash);

        db.collection("facilities")
                .document(facilityId)
                .collection("My Events")
                .document(eventName)
                .update(qrCodeData)
                .addOnSuccessListener(aVoid -> Toast.makeText(CreateEventActivity.this, "QR Code hash stored successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Error storing QR Code hash", Toast.LENGTH_SHORT).show());
    }
}
