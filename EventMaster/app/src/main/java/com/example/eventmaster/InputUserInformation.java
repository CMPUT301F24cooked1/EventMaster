package com.example.eventmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for user to enter all their personal information, name, email, phone number(optional), profile picture
 */
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
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PROFILE_COLOR_KEY = "profile_color";
    private Profile user;
    private TextView remove_pfp;
    private static final String FIREBASE_PROFILE_PIC_KEY = "profilePictureUrl";
    private FirebaseStorage storage;
    private FirebaseFirestore db;


    /**
     * Initialize the profile entry screen where user can enter their personal information
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.edit_profile_screen);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        user = (Profile) getIntent().getSerializableExtra("User");

        //input boxes for user
        name_edit = findViewById(R.id.edit_name);
        email_edit = findViewById(R.id.edit_email);
        phone_number_edit = findViewById(R.id.edit_phone_number);
        profile_change_button = findViewById(R.id.save_changes_button);
        profile_picture = findViewById(R.id.profile_picture);
        upload_profile_picture = findViewById(R.id.upload_profile_picture);

        // Saves the user's input
        name_edit.setText(user.getName());
        email_edit.setText(user.getEmail());
        phone_number_edit.setText(user.getPhone_number());
        ProfilePicture.loadProfilePicture(user, profile_picture, InputUserInformation.this);

        // Set the changes and finish the activity, results are sent back to ProfileActivity
        profile_change_button.setOnClickListener(v -> {
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
            updateUserInfo(user.getDeviceId(), user.getName(), user.getEmail(), user.getPhone_number(), () -> {
                ProfilePicture.loadProfilePicture(user, profile_picture, InputUserInformation.this);
                Intent intent = new Intent(InputUserInformation.this, ProfileActivity.class);
                intent.putExtra("User", user);
                setResult(RESULT_OK, intent);
                finish();
            });
        });

        // results after user uploads their profile picture
        profileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                Bitmap croppedPFP = Image.cropProfilePicture(selectedImage, 90);
                                profile_picture.setImageBitmap(croppedPFP);

                                Image.saveProfilePicture(croppedPFP, InputUserInformation.this);
                                uploadProfilePictureToFirebase(croppedPFP);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        // Allows users to pick their profile picture
        upload_profile_picture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            profileActivityResultLauncher.launch(intent);
        });

        // User can click "remove pfp" to remove their uploaded photo
        remove_pfp = findViewById(R.id.remove_profile_picture);
        remove_pfp.setOnClickListener(v -> removeProfilePicture());



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
    private void updateUserInfo(String deviceId, String name, String email, String phone_number, Runnable onComplete) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone number", phone_number);

        db.collection("profiles").document(deviceId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User info updated successfully.");
                    onComplete.run(); // Finish activity after successful update
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating user info", e);
                    Toast.makeText(this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Uploads the profile picture to Firebase Storage and saves the download URL in Firestore.
     *
     * @param bitmap The profile picture bitmap.
     */
    private void uploadProfilePictureToFirebase(Bitmap bitmap) {
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create a reference to the Firebase Storage location
        StorageReference profilePicRef = storage.getReference().child("profile_pictures/" + user.getDeviceId() + "_profile_picture.png");

        // Upload the image to Firebase Storage
        UploadTask uploadTask = profilePicRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                saveProfilePictureUrlToFirestore(downloadUrl);
            }).addOnFailureListener(e -> Log.e("FirebaseStorage", "Failed to get download URL", e));
        }).addOnFailureListener(e -> Log.e("FirebaseStorage", "Failed to upload image", e));
    }

    /**
     * Saves the profile picture URL to Firestore under the user's document.
     *
     * @param downloadUrl The download URL of the profile picture.
     */
    private void saveProfilePictureUrlToFirestore(String downloadUrl) {
        db.collection("profiles").document(user.getDeviceId())
                .update(FIREBASE_PROFILE_PIC_KEY, downloadUrl)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Profile picture URL saved successfully."))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving profile picture URL", e));
    }

    /**
     * When user removes their profile picture, it gets removed off shared preference and sets their profile to their default one.
     * It also gets deleted off firestore and firebase.
     */
    private void removeProfilePicture() {
        // Removes the photo from saved preference
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("profile_picture_bitmap");
        editor.apply();

        // Get the saved color from shared prefernce to set their auto generated pfp
        int savedColor = sharedPreferences.getInt(PROFILE_COLOR_KEY, -1);
        if (savedColor == -1) {
            savedColor = ProfilePicture.randomColorGenerator();
            editor.putInt(PROFILE_COLOR_KEY, savedColor); // Save the newly generated color
            editor.apply();
        }

        // Generate the default profile picture using the saved or generated color
        if (user.getName().isEmpty()){
            profile_picture.setImageResource(R.drawable.profile_picture);
        }
        else{
            Bitmap defaultPicture = Image.generateProfilePicture(user.getName(), 200, savedColor, Color.WHITE);
            profile_picture.setImageBitmap(defaultPicture);
        }

        // Delete the profile picture from Firebase Storage
        StorageReference profilePicRef = FirebaseStorage.getInstance().getReference()
                .child("profile_pictures/" + user.getDeviceId() + "_profile_picture.png");
        profilePicRef.delete().addOnSuccessListener(aVoid -> {
            Log.d("FirebaseStorage", "Profile picture deleted from Firebase Storage");
            // Remove the profile picture URL from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("profiles").document(user.getDeviceId())
                    .update("profilePictureUrl", null) // Set URL to null to indicate no profile picture
                    .addOnSuccessListener(aVoid1 -> {
                        Log.d("Firestore", "Profile picture URL removed from Firestore");
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error removing profile picture URL", e));
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Error deleting profile picture from Firebase Storage", e);
        });
    }

    /**
     * Makes sure the phone number is valid with only integers
     * @param phone_number
     */
    private void validatePhoneNumber(String phone_number) {
        String trimmedPhoneNumber = phone_number.trim();
        if (trimmedPhoneNumber.isEmpty()) {
            return;
        }
        if (!trimmedPhoneNumber.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("Phone number should contain only digits.");
        }
    }


}
