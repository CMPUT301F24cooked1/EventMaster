package com.example.eventmaster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore firestore;
    private String deviceId;
    private String eventName;
    private static final String TAG = "MapActivity";
    private static final String GEOCODING_API_KEY = "AIzaSyC9bWvuWrlCcA-cMGNF1tib2l43cQfh5Yk"; // Replace with your actual API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get extras passed from the previous activity
        deviceId = getIntent().getStringExtra("deviceId");
        eventName = getIntent().getStringExtra("eventName");
        // Back button click listener
        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish()); // Exits the activity

        // Initialize the Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error loading map fragment.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        fetchAndDisplayCoordinates();
        fetchEventAddressAndGeocode();
    }

    private void fetchEventAddressAndGeocode() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document Data: " + document.getData());
                            String eventVenue = document.getString("eventVenue");
                            Log.d(TAG, "Event Venue String:"+ eventVenue);
                            if (eventVenue != null) {
                                Log.d(TAG, "Event Venue Null");
                                geocodeAddress(eventVenue);
                            } else {
                                Log.e(TAG, "Event address not found in document.");
                                Toast.makeText(this, "Event address not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Event document does not exist.");
                            Toast.makeText(this, "Event does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error fetching event document", task.getException());
                        Toast.makeText(this, "Failed to load event address.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void geocodeAddress(String address) {
        Log.d(TAG, "Geocoding Address: " + address);

        Geocoder geocoder = new Geocoder(this);
        try {
            // Geocode the address
            List<Address> results = geocoder.getFromLocationName(address, 1);

            if (results != null && !results.isEmpty()) {
                Address location = results.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Log.d(TAG, "Geocoded Location: Lat " + latitude + ", Lng " + longitude);

                // Display marker on map
                LatLng latLng = new LatLng(latitude, longitude);
                showEventMarker(latLng, address);
            } else {
                Log.e(TAG, "No geocoding results found for: " + address);
                Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoding failed: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to geocode address.", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchAndDisplayCoordinates() {
        firestore.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventName)
                .collection("unsampled list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<LatLng> coordinates = new ArrayList<>();
                        List<String> deviceIds = new ArrayList<>();

                        // Collect coordinates and deviceIds
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");
                            String attendeeDeviceId = document.getString("entrantId");

                            // Log the latitude, longitude, and deviceId values
                            Log.d("MapActivity", "Fetched Latitude: " + latitude + ", Longitude: " + longitude + ", DeviceId: " + attendeeDeviceId);

                            if (latitude != null && longitude != null && attendeeDeviceId != null) {
                                coordinates.add(new LatLng(latitude, longitude));
                                deviceIds.add(attendeeDeviceId);
                            } else {
                                Log.d("MapActivity", "Latitude, Longitude, or DeviceId is null for document: " + document.getId());
                            }
                        }

                        // Fetch names for the deviceIds and show markers on the map
                        if (!coordinates.isEmpty() && !deviceIds.isEmpty()) {
                            fetchNamesAndShowMarkers(coordinates, deviceIds);
                        } else {
                            Toast.makeText(this, "No coordinates or device IDs found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("MapActivity", "Error fetching coordinates", task.getException());
                        Toast.makeText(this, "Failed to load coordinates.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchNamesAndShowMarkers(List<LatLng> coordinates, List<String> deviceIds) {
        for (int i = 0; i < deviceIds.size(); i++) {
            String deviceId = deviceIds.get(i);
            LatLng location = coordinates.get(i);

            // Fetch the name for the current attendee using their deviceId
            firestore.collection("profiles")
                    .document(deviceId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String attendeeName = document.getString("name");

                                if (attendeeName != null) {
                                    Log.d("MapActivity", "Attendee Name: " + attendeeName);

                                    // Show the marker with the attendee's name as the title
                                    mMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .title(attendeeName));  // Use attendee's name as the marker title
                                } else {
                                    Log.d("MapActivity", "Name not found for deviceId: " + deviceId);
                                    // If no name, you can use a fallback title
                                    mMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .title("Unnamed Attendee"));
                                }
                            } else {
                                Log.d("MapActivity", "No profile found for deviceId: " + deviceId);
                            }
                        } else {
                            Log.e("MapActivity", "Error fetching profile for deviceId: " + deviceId, task.getException());
                        }
                    });
        }
    }

    private Bitmap resizeMarkerIcon(int drawableId, int width, int height) {
        // Get the drawable resource
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);

        // Convert the drawable to a Bitmap
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        // Resize the Bitmap
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private void showMarkersOnMap(List<LatLng> coordinates) {
        for (LatLng location : coordinates) {
            mMap.addMarker(new MarkerOptions().position(location).title("Attendee Location"));
        }

    }

    private void showEventMarker(LatLng eventLocation, String address) {
        Bitmap resizedIcon = resizeMarkerIcon(R.drawable.star, 180, 190); // Adjust size as needed

        // Add a marker for the event location with the resized star icon
        mMap.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title("Event Location")
                .snippet(address)
                .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon))); // Set the resized icon

        // Move the camera to the event location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 12));
    }


}
