package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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
    private ActivityResultLauncher<Intent> profileActivityResultLauncher;
    private FirebaseFirestore db;



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

        name_edit.setText(user.getName());
        email_edit.setText(user.getEmail());
        phone_number_edit.setText(user.getPhone_number());

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

}
