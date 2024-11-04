package com.example.eventmaster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.URL;

/**
 * The class provides methods for handling the user's profile picture.
 * It generates a default profile picture with the help of the ProfilePicture class.
 * It crops the uploaded profile picture from the user and stores it into firebase,
 * setting it using Glide.
 *
 * <p> This class also generates a unique ID based on the device ID</p>
 *
 */
public class Image {
    private String deviceID;
    private String imageName;
    private String imageID;

    public Image(String deviceID, String imageName){
        this.deviceID = deviceID;
        this.imageName = imageName;
    }

    /**
     * Returns the image ID; if there is no image ID, it will create one and return it
     * Concatenates the device ID with the image ID to create the full image ID
     * @return imageID
     */
    public String getImageID() {
        if (imageID == null){
            imageID = deviceID + "_" + generateImageID();
        }
        return imageID;
    }

    /**
     * Generates a unique string ID for an image
     * The length of the ID is fixed at 10
     * @return result the unique string ID
     */
    private String generateImageID(){
        int length = 10;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            result.append(characters.charAt(index));
        }

        return result.toString();
    }



    /**
     * Creates a bitmap of the profile picture that is based on the name of the User
     *
     * @param name the user's name
     * @param imageSize the size of the image
     * @param backgroundColor the profile picture color
     * @param textColor the text color
     *
     * @return the bitmap of the created profile picture for the user
     */
    public static Bitmap generateProfilePicture(String name, int imageSize, int backgroundColor, int textColor) {
        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        // Set background to the background color
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);

        // Draws a circle
        float radius = imageSize / 2f;
        canvas.drawCircle(radius, radius, radius, backgroundPaint);

        // Changes the color and sets text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(imageSize * 0.5f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Get the first letter of the name
        String firstLetter = name.substring(0, 1).toUpperCase();

        // Measure the text size
        Rect textBounds = new Rect();
        textPaint.getTextBounds(firstLetter, 0, firstLetter.length(), textBounds);

        // Center the text position
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        // Draw the letter on the canvas
        canvas.drawText(firstLetter, xPos, yPos, textPaint);

        return bitmap; // Return the bitmap
    }

    /**
     * Creates a bitmap of the profile picture that is based on the uploaded photo
     *
     * @param source the bitmap of the image
     * @param desiredRadius the radius of the profile picture that we'll crop
     *
     * @return the bitmap of the uploaded profile picture for the user
     */
    public static Bitmap cropProfilePicture(Bitmap source, float desiredRadius) {
        // Calculate diameter
        int diameter = (int) (desiredRadius * 2);

        // Create a new Bitmap
        Bitmap profilePicture = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(profilePicture);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw the circular crop
        Path path = new Path();
        path.addCircle(desiredRadius, desiredRadius, desiredRadius, Path.Direction.CCW);
        canvas.clipPath(path);

        // Calculate the center of the image
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int centerX = sourceWidth / 2;
        int centerY = sourceHeight / 2;

        int cropSize = Math.min(sourceWidth, sourceHeight);
        int left = centerX - (cropSize / 2);
        int top = centerY - (cropSize / 2);
        int right = centerX + (cropSize / 2);
        int bottom = centerY + (cropSize / 2);

        Rect srcRect = new Rect(left, top, right, bottom);

        RectF destRect = new RectF(0, 0, diameter, diameter);

        // Draw the cropped photo into the circular area & scale it to fit the diameter
        canvas.drawBitmap(source, srcRect, destRect, paint);

        return profilePicture;
    }

    /**
     * Sets the uploaded profile picture to the Imageview VIA Glide
     *
     * @param profilePictureUrl the URL of the image
     * @param profilePicture the imageview ID of the profile picture
     * @param context the context of which activity screen it's being ran on
     *
     */
    public static void setProfilePicture(String profilePictureUrl, ImageView profilePicture, Context context) {
        // Use Glide to load the profile picture into the ImageView
        Glide.with(context)
                .load(profilePictureUrl)
                .circleCrop()  // Crop it
                .into(profilePicture);
    }

    /**
     * Checks if there's a profile picture URL in firestore
     * Sets the uploaded profile picture / default profile picture depending on whether the user has uploaded an image or has set a name
     *
     * @param deviceID the device ID of the user
     * @param user the user object of the device ID
     * @param profilePicture the imageview ID of the profile picture
     * @param context the context of which activity screen it's being ran on
     *
     */
    public static void getProfilePictureUrl(String deviceID, Profile user, ImageView profilePicture, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userDocRef = db.collection("profiles").document(deviceID);

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get the profile picture URL from Firestore
                    String profilePictureUrl = document.getString("profilePictureUrl");

                    // Check firestore to see if the user has uploaded a PFP
                    if (profilePictureUrl == null || profilePictureUrl.isEmpty()) {
                        ProfilePicture.generateProfilePicture(user, profilePicture, context);
                        Log.d("Firestore", "User has not uploaded a profile picture.");
                    } else { // if not call setProfilePicture to set default ones
                        Log.d("Firestore", "User has uploaded a profile picture.");
                        setProfilePicture(profilePictureUrl, profilePicture, context);
                    }
                } else { // if not call setProfilePicture to set default ones
                    ProfilePicture.generateProfilePicture(user, profilePicture, context);
                    Log.d("Firestore", "No such document.");
                }
            } else { // if not call setProfilePicture to set default ones
                Log.e("Firestore", "Failed to retrieve profile picture URL", task.getException());
                ProfilePicture.generateProfilePicture(user, profilePicture, context);
            }
        });
    }









}
