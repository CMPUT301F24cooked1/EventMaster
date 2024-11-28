package com.example.eventmaster;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventmaster.Facility;
import com.example.eventmaster.MainActivity;
import com.example.eventmaster.ModeActivity;
import com.example.eventmaster.Notifications;
import com.example.eventmaster.Profile;
import com.example.eventmaster.ProfileActivity;
import com.example.eventmaster.QRScanFragment;
import com.example.eventmaster.R;
import com.example.eventmaster.SettingsScreen;
import com.example.eventmaster.ViewFacilitiesAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.AbstractMap;

import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Displays all of the facilities that have been created
 */
public class AdminQRActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViewQRAdapter viewQRAdapter;
    private ArrayList<AbstractMap.SimpleEntry<String, String>> qrList;
    private FirebaseFirestore firestore;
    private String deviceId; // Replace with actual device ID
    private Button deleteButton;
    private boolean isDeleteMode = false;
    private Profile user;

    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private ActivityResultLauncher<Intent> QRScanScreenResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.admin_qrcode_screen);
        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("User");

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        qrList = new ArrayList<>();

        viewQRAdapter = new ViewQRAdapter(qrList, this, user, true);
        recyclerView.setAdapter(viewQRAdapter);

        deleteButton = findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(v -> {
            if (!isDeleteMode) {
                viewQRAdapter.toggleCheckBoxVisibility();
                viewQRAdapter.notifyDataSetChanged();
                deleteButton.setText("Delete Items");
            } else {
                //delete checked items
                viewQRAdapter.deleteSelectedQRs();
                viewQRAdapter.toggleCheckBoxVisibility();
                viewQRAdapter.notifyDataSetChanged();
                deleteButton.setText("Select to Delete");
            }
            isDeleteMode = !isDeleteMode; // Toggle mode

        });

        // Retrieve events from Firestore
        retrieveQRHashCodes();


        // Initialize navigation buttons
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
                            user=updatedUser; // Apply the updated Profile to MainActivity's user
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
                            user=updatedUser; // Apply the updated Profile to MainActivity's user
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
                newIntent = new Intent(AdminQRActivity.this, MainActivity.class);
                newIntent.putExtra("User", user);
                MainActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Settings) {
                newIntent = new Intent(com.example.eventmaster.AdminQRActivity. this, SettingsScreen.class);
                newIntent.putExtra("User", user);
                settingsResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Notifications) {
                newIntent = new Intent(com.example.eventmaster.AdminQRActivity.this, Notifications.class);
                newIntent.putExtra("User", user);
                notificationActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_Profile) {
                newIntent = new Intent(com.example.eventmaster.AdminQRActivity.this, ProfileActivity.class);
                newIntent.putExtra("User", user);
                ProfileActivityResultLauncher.launch(newIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_scan_qr) {
                // Open QRScanFragment without simulating button click
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
     * Gets the string url of the images, and saves them into imageList, to transfer to ViewImagesAdapter
     */
    private void retrieveQRHashCodes() {
        CollectionReference facilitiesRef = firestore.collection("facilities");

        facilitiesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot facilityDoc : task.getResult()) {

                    // retrieve events for each facility
                    CollectionReference eventsRef = facilityDoc.getReference().collection("My Events");


                    eventsRef.get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                String eventName = eventDoc.getString("eventName");
                                String qrHash = eventDoc.getString("hash");
                                qrList.add(new AbstractMap.SimpleEntry<>(eventName, qrHash));
                                Log.d("AdminQR", "retrieveQRHashCodes: " + eventName);
                                Log.d("AdminQR", "retrieveQRHashCodes: " + qrHash);
                            }
                            viewQRAdapter.notifyDataSetChanged(); // notify the adapter of data changes

                        } else {
                            Log.d("JoinEventScreen", "QuerySnapshot is null");
                        }
                    });

                }} else {
                Log.e("JoinEventScreen", "Error getting documents: ", task.getException());
            }
        });
    }
}

