package com.example.eventmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * The Image class provides functionality for managing and changing a profile picture.
 * It supports generating a default profile picture based on a user's name, cropping an uploaded picture,
 * and saving or retrieving images from SharedPreferences as Base64 strings.
 */
public class Image {

    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PROFILE_PICTURE_KEY = "profile_picture_bitmap";

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
     * Saves a profile picture Bitmap to SharedPreferences as a Base64 string.
     *
     * @param bitmap  the Bitmap of the profile picture
     * @param context the current context
     */
    public static void saveProfilePicture(Bitmap bitmap, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Encode Bitmap to Base64
        String encodedImage = encodeBitmapToBase64(bitmap);
        editor.putString(PROFILE_PICTURE_KEY, encodedImage);
        editor.apply();
        Log.d("Image", "Profile picture saved to SharedPreferences.");
    }

    /**
     * Generates a default profile picture Bitmap based on the user's name.
     *
     * @param name            the user's name, used to extract the initial
     * @param imageSize       the size of the generated image
     * @param backgroundColor the background color of the profile picture
     * @param textColor       the color of the initial text
     * @return a Bitmap representing the generated profile picture
     */
    public static Bitmap generateProfilePicture(String name, int imageSize, int backgroundColor, int textColor) {

        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        float radius = imageSize / 2f;
        canvas.drawCircle(radius, radius, radius, backgroundPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(imageSize * 0.5f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        String firstLetter = name.substring(0, 1).toUpperCase();
        Rect textBounds = new Rect();
        textPaint.getTextBounds(firstLetter, 0, firstLetter.length(), textBounds);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawText(firstLetter, xPos, yPos, textPaint);

        return bitmap;
    }

    /**
     * Crops an uploaded profile picture Bitmap into a circular shape.
     *
     * @param source       the source Bitmap
     * @param desiredRadius the radius for the circular crop
     * @return a circular cropped Bitmap
     */
    public static Bitmap cropProfilePicture(Bitmap source, float desiredRadius) {
        int diameter = (int) (desiredRadius * 2);
        Bitmap profilePicture = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(profilePicture);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Path path = new Path();
        path.addCircle(desiredRadius, desiredRadius, desiredRadius, Path.Direction.CCW);
        canvas.clipPath(path);

        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int cropSize = Math.min(sourceWidth, sourceHeight);
        Rect srcRect = new Rect(sourceWidth / 2 - cropSize / 2, sourceHeight / 2 - cropSize / 2,
                sourceWidth / 2 + cropSize / 2, sourceHeight / 2 + cropSize / 2);
        RectF destRect = new RectF(0, 0, diameter, diameter);

        canvas.drawBitmap(source, srcRect, destRect, paint);

        return profilePicture;
    }

    /**
     * Encodes a Bitmap into a Base64 string.
     *
     * @param bitmap the Bitmap to encode
     * @return the Base64 string representation of the Bitmap
     */
    public static String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    /**
     * Decodes a Base64 string into a Bitmap.
     *
     * @param encodedImage the Base64 string to decode
     * @return the decoded Bitmap
     */
    public static Bitmap decodeBase64ToBitmap(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
