package com.example.eventmaster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InputUserInformation extends AppCompatActivity {
    private EditText name_edit;
    private EditText email_edit;
    private EditText password_edit;
    private EditText phone_number_edit;
    private Button profile_change_button;
    private ImageButton back_button;
    private ImageView profile_picture;
    private ImageButton upload_profile_picture;
    private ActivityResultLauncher<Intent> profileActivityResultLauncher;
    private FirebaseFirestore db;
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PROFILE_COLOR_KEY = "profile_color";
    private byte[] byteArrayPFP;



    /**
     * Initializes the input user information screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * Allows the user to set their name, email, phone number
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        db = FirebaseFirestore.getInstance();

        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity


        name_edit = findViewById(R.id.edit_name);
        email_edit = findViewById(R.id.edit_email);
        phone_number_edit = findViewById(R.id.edit_phone_number);
        profile_change_button = findViewById(R.id.save_changes_button);

        profile_picture = findViewById(R.id.profile_picture);
        upload_profile_picture = findViewById(R.id.upload_profile_picture);

        name_edit.setText(user.getName());
        email_edit.setText(user.getEmail());
        phone_number_edit.setText(user.getPhone_number());
        Image.getProfilePictureUrl(user.getDeviceId(), user, profile_picture, InputUserInformation.this);

        profile_change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_edit.getText().toString();
                String email = email_edit.getText().toString();
                String phone_number = phone_number_edit.getText().toString();
                user.setName(name);
                user.setEmail(email);
                try {
                    validatePhoneNumber(phone_number);
                    user.setPhone_number(phone_number);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(InputUserInformation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                updateUserInfo(user.getDeviceId(), user.getName(), user.getEmail(), user.getPhone_number());
                Intent intent = new Intent(InputUserInformation.this, ProfileActivity.class);
                intent.putExtra("User", user);
                profileActivityResultLauncher.launch(intent);
            }
        });

        profileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        // launches intent when user uploads their profile picture
        upload_profile_picture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            profileActivityResultLauncher.launch(intent);
        });

        profileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                // Get the input stream of the selected image
                                InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                                uploadProfileFirebase(user.getDeviceId(), selectedImage);

                                // Crop the image into a circle with a radius of 100
                                Bitmap croppedPFP = Image.cropProfilePicture(selectedImage, 100);
                                profile_picture.setImageBitmap(croppedPFP);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    private void validatePhoneNumber(String phone_number) {
        String trimmedPhoneNumber = phone_number.trim(); // Remove leading/trailing whitespace
        if (trimmedPhoneNumber.isEmpty()) {
            return;
        }
        if (!trimmedPhoneNumber.matches("^[0-9]+$")) { // Ensure only digits
            throw new IllegalArgumentException("Phone number should contain only digits.");
        }
    }

    /**
     * Updates firestore with the user's information
     *
     * @param deviceId the deviceID of the user
     * @param name the name of the user
     * @param email the email of the user
     * @param phone_number the phone number of the user
     *
     */
    private void updateUserInfo(String deviceId, String name, String email, String phone_number) {
        // Create a map with the additional user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone number", phone_number);

        // Update the document with the new user data, merging with existing data
        db.collection("profiles")
                .document(deviceId)
                .set(userData, SetOptions.merge()) // Merge with existing data
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User info updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating user info", e);
                });
    }

    /**
     * Update firebase with the uploaded profile picture
     *
     * @param deviceId the deviceID of the user
     * @param bitmap the bitmap of the image
     *
     */
    private void uploadProfileFirebase(String deviceId, Bitmap bitmap) {
        // Convert the Bitmap to a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference where the profile picture will be stored under "profile_pictures" folder
        StorageReference profilePictureRef = storageRef.child("profile_pictures/" + deviceId + "_profile_picture.png");

        // Upload the image to Firebase Storage
        UploadTask uploadTask = profilePictureRef.putBytes(imageData);

        // Check if the upload to firebase was successful or not & save it to Firestore
        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the download URL if upload was successful
                profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save the download URL to Firestore
                    saveProfilePictureFirestore(deviceId, uri.toString());
                }).addOnFailureListener(e -> {
                    // Handle failure to get the download URL
                    Log.e("FirebaseStorage", "Error getting download URL", e);
                });
            } else {
                // Handle failure to upload the image
                Log.e("FirebaseStorage", "Error uploading image", task.getException());
            }
        });
    }

    /**
     * Update Firestore with the URL of the image that is stored in Firebase
     *
     * @param deviceId the deviceID of the user
     * @param downloadUrl the URL of the image
     *
     */
    private void saveProfilePictureFirestore(String deviceId, String downloadUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map to store the image's URL
        Map<String, Object> userProfileUpdate = new HashMap<>();
        userProfileUpdate.put("profilePictureUrl", downloadUrl);

        // Update the user's document in Firestore with the profile picture URL
        db.collection("profiles").document(deviceId)
                .set(userProfileUpdate, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Profile picture URL saved successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving profile picture URL", e);
                });
    }


}
