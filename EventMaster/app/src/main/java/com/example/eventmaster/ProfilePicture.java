package com.example.eventmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * The class provides methods of generating a default profile picture. It includes generating
 * a random color for the profile picture and creates a default profile picture bitmap based
 * on the user's name.
 *
 * <p> The class interacts with SharedPreferences to store and retrieve the random color
 * that has been selected. This ensures consistency every time the user goes on the app. </p>
 */
public class ProfilePicture {
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PROFILE_COLOR_KEY = "profile_color";
    /**
     * Random generates a color for the background of the profile picture
     *
     * @return a color that isn't close to white
     */
    public static int randomColorGenerator() {
        Random random = new Random();

        int red, green, blue;

        do {
            red = random.nextInt(256);
            green = random.nextInt(256);
            blue = random.nextInt(256);
        } while ((red + green + blue) > 600); // Makes sure that the background color won't be close to the text color white

        return Color.rgb(red, green, blue);
    }
    /**
     * Sets a default profile picture based on the user's name
     * @param user the user object
     * @param profile_picture the ImageView ID of the profile picture
     * @param context the context of which activity screen it's being ran on
     *
     */
    public static void generateProfilePicture(Profile user, ImageView profile_picture, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedColor = prefs.getInt(PROFILE_COLOR_KEY, -1);

        // Check if the name is empty
        if (user.getName().isEmpty()) {
            profile_picture.setImageResource(R.drawable.profile_picture); // set to default profile picture if name is empty
        } else {
            if (savedColor == -1) {
                // If no color is saved, generate a new random background color
                int newColor = randomColorGenerator(); // Generates a random color

                // Save the new color in SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PROFILE_COLOR_KEY, newColor);
                editor.apply();

                savedColor = newColor; // Use the new generated color
            }

            // Generate the profile picture bitmap with the saved color or the new generated color
            Bitmap pfpBitmap = Image.generateProfilePicture(user.getName(), 200, savedColor, Color.WHITE);
            profile_picture.setImageBitmap(pfpBitmap);
        }
    }
}
