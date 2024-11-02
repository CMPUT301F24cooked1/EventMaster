package com.example.eventmaster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScanFragment extends AppCompatActivity{
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_qr_screen);

        AppCompatButton scanButton = findViewById(R.id.scan_qr_code_button);
        Button button = findViewById(R.id.next_button);

        // click on the scan button
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // implement scanner inside here
                ScanCode();

            }
        });

        // Button to skip QR scanning WILL DELETE THIS LATER!!!
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String event = intent.getStringExtra("event");
                String deviceID = intent.getStringExtra("deviceID");
                fetchEventData(deviceID, event);
            }
        });

    }

    private void ScanCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        options.setCaptureActivity(CaptureActivity.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null)
        {
            // information from ViewEventsAdapter
            String scannedHash = result.getContents();
            Intent intent = getIntent();
            String event = intent.getStringExtra("event");
            String deviceID = intent.getStringExtra("deviceID");  // facility device id

            // send information over to retrieveEventInfo
            Intent intent2 = new Intent(QRScanFragment.this, retrieveEventInfo.class);
            intent2.putExtra("HASHED_DATA", scannedHash);
            intent2.putExtra("event", event);
            intent2.putExtra("deviceID", deviceID);  // facility device id

            startActivity(intent2);

        }
    });


    // WILL DELETE LATER its for a button to go straight to the next screen without scanning
    private void fetchEventData(String deviceID, String event) {
        db.collection("facilities")
                .document(deviceID)
                .collection("My Events")
                .whereEqualTo("eventName", event) // Adjust the query as necessary
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String hashedData = document.getString("hash");
                                String eventDescription = document.getString("eventDescription");
                                String eventPosterUrl = document.getString("posterUrl");

                                Intent intent2 = new Intent(QRScanFragment.this, retrieveEventInfo.class);
                                intent2.putExtra("hashed_data", hashedData);
                                intent2.putExtra("event", event);
                                intent2.putExtra("deviceID", deviceID);
                                intent2.putExtra("eventDescription", eventDescription);
                                intent2.putExtra("posterUrl", eventPosterUrl);

                                startActivity(intent2);

                            }
                        } else {
                            Toast.makeText(QRScanFragment.this, "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(QRScanFragment.this, "Error getting data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



}


