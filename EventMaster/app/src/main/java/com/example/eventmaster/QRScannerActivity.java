package com.example.eventmaster;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private DecoratedBarcodeView barcodeScanner;
    private Profile user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        barcodeScanner = findViewById(R.id.barcode_scanner);
        user = (Profile) getIntent().getSerializableExtra("User");  // the user information is passed in

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                //Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
                finish(); // Exit activity if permission is denied
            }
        }
    }

    private void startScanner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String scannedData = result.getText();
                if (scannedData != null) {
                    barcodeScanner.pause(); // Stop scanning
                    handleScannedData(scannedData);
                }
            }
        });
    }
    private void handleScannedData(String scannedData) {
        try {
            // The scannedData should contain the hash (not the full event URL)
            String hash = scannedData;  // This is the hash from the QR code

            // Initialize Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Query Firestore to get all facilities
            db.collection("facilities")
                    .get() // Get all documents in the "facilities" collection
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Iterate through all the facilities
                            for (DocumentSnapshot facilityDoc : queryDocumentSnapshots.getDocuments()) {
                                // Get the facilityId
                                String facilityId = facilityDoc.getId();

                                // Query the "My Events" collection for this facility
                                db.collection("facilities")
                                        .document(facilityId)
                                        .collection("My Events")
                                        .get() // Get all events for this facility
                                        .addOnSuccessListener(eventQueryDocumentSnapshots -> {
                                            if (!eventQueryDocumentSnapshots.isEmpty()) {
                                                // Iterate through the events for this facility
                                                for (DocumentSnapshot eventDoc : eventQueryDocumentSnapshots.getDocuments()) {
                                                    // Check if the hash matches
                                                    if (eventDoc.contains("hash") && eventDoc.getString("hash").equals(hash)) {
                                                        // Hash matched, extract event details
                                                        String eventName = eventDoc.getString("eventName");

                                                        // create new intent to send information over to the retrieveEventInfo class
                                                        Intent intent2 = new Intent(QRScannerActivity.this, retrieveEventInfo.class);
                                                        intent2.putExtra("hashed_data", hash);
                                                        intent2.putExtra("event", eventName);
                                                        intent2.putExtra("deviceID", facilityId);  // facility device id
                                                        intent2.putExtra("User", user);
                                                        startActivity(intent2);
                                                    }
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            //Toast.makeText(this, "Error retrieving events for facility " + facilityId, Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            //Toast.makeText(this, "No facilities found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        //Toast.makeText(this, "Error retrieving facilities.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();  // Log the error for debugging
                    });
        } catch (Exception e) {
            // Handle any exceptions (including URI parsing issues)
            //Toast.makeText(this, "Invalid QR Code format.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            barcodeScanner.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }
}
