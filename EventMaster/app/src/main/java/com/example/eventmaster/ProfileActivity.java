package com.example.eventmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class ProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PROFILE_COLOR_KEY = "profile_color";

    private TextView displayName;
    private TextView displayEmail;
    private TextView displayPhoneNumber;
    private AppCompatButton editProfileButton;
    private ImageButton backButton;
    private ActivityResultLauncher<Intent> editProfileResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;

    /**
     * Initializes the profile screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * Sets the user's inputted information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_screen);
        Profile user = (Profile) getIntent().getSerializableExtra("User"); // User from MainActivity
        ImageView profile_picture = findViewById(R.id.profile_picture );

        Image.getProfilePictureUrl(user.getDeviceId(), user, profile_picture, ProfileActivity.this);

        displayName = findViewById(R.id.profile_name);
        displayEmail = findViewById(R.id.profile_email);
        displayPhoneNumber = findViewById(R.id.profile_phone_number);
        editProfileButton = findViewById(R.id.edit_profile_button);
        backButton = findViewById(R.id.back);

        displayName.setText(user.getName());
        displayEmail.setText(user.getEmail());
        displayPhoneNumber.setText(user.getPhone_number());

        editProfileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, InputUserInformation.class);
                intent.putExtra("User", user);
                editProfileResultLauncher.launch(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("User", user);
                MainActivityResultLauncher.launch(intent);
            }
        });

    }

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
