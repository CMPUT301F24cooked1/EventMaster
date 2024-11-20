package com.example.eventmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * ProfilePicture provides methods to manage a user's profile picture, either by loading
 * it from SharedPreferences or generating a default profile picture
 */
public class ProfilePicture {
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PROFILE_COLOR_KEY = "profile_color";
    private static final String PROFILE_PICTURE_KEY = "profile_picture_bitmap";

    /**
     * Generates a random color that is not too close to white.
     * @return a color that is suitable as a profile background.
     */
    public static int randomColorGenerator() {
        Random random = new Random();
        int red, green, blue;
        do {
            red = random.nextInt(256);
            green = random.nextInt(256);
            blue = random.nextInt(256);
        } while ((red + green + blue) > 600); // Avoid colors too close to white
        return Color.rgb(red, green, blue);
    }

    /**
     * Loads the profile picture from SharedPreferences if it exists; otherwise generates a default one.
     *
     * @param user the user object
     * @param profilePicture the ImageView to display the picture
     * @param context the context to access SharedPreferences
     */
    public static void loadProfilePicture(Profile user, ImageView profilePicture, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encodedImage = sharedPreferences.getString(PROFILE_PICTURE_KEY, null);

        if (encodedImage != null) {
            Bitmap bitmap = Image.decodeBase64ToBitmap(encodedImage);
            profilePicture.setImageBitmap(bitmap);
        } else {
            generateProfilePicture(user, profilePicture, context);
        }
    }

    /**
     * Generates a profile picture with a background color, based on the user's name.
     *
     * @param user the user object
     * @param profilePicture the ImageView to display the picture
     * @param context the context to access SharedPreferences
     */
    public static void generateProfilePicture(Profile user, ImageView profilePicture, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int color = prefs.getInt(PROFILE_COLOR_KEY, -1);

        if (color == -1) {
            color = randomColorGenerator();
            prefs.edit().putInt(PROFILE_COLOR_KEY, color).apply();
        }

        Bitmap pfpBitmap;
        if (user.getName().isEmpty()) {
            profilePicture.setImageResource(R.drawable.profile_picture);
        } else {
            pfpBitmap = Image.generateProfilePicture(user.getName(), 180, color, Color.WHITE);
            profilePicture.setImageBitmap(pfpBitmap);
            // Upload the generated profile picture to Firebase
            uploadGeneratedProfilePictureToFirebase(user, pfpBitmap);
        }
    }

    /**
     * Uploads the generated profile picture to Firebase Storage.
     *
     * @param user the user object
     * @param bitmap the generated profile picture bitmap
     */
    private static void uploadGeneratedProfilePictureToFirebase(Profile user, Bitmap bitmap) {
        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Get the Firebase Storage reference for the profile picture
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference profilePicRef = storage.getReference().child("profile_pictures/" + user.getDeviceId() + "_profile_picture.png");

        // Upload the profile picture
        profilePicRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("FirebaseStorage", "Generated profile picture uploaded successfully.");
                    // Optionally, update Firestore with the download URL
                    profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("profiles").document(user.getDeviceId())
                                .update("profilePictureUrl", uri.toString())
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Profile picture URL updated successfully."))
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update profile picture URL", e));
                    }).addOnFailureListener(e -> Log.e("FirebaseStorage", "Failed to retrieve download URL", e));
                })
                .addOnFailureListener(e -> Log.e("FirebaseStorage", "Failed to upload generated profile picture", e));
    }

}
