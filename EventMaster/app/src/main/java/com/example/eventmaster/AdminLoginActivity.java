package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.auth.User;

/**
 *  Ensures the person accessing admin privileges has the correct login information
 */
public class AdminLoginActivity extends AppCompatActivity {
    // A hardcoded password
    final String adminPassword = "123456";
    private EditText adminCodeInput;
    private ActivityResultLauncher<Intent> adminMainResultLauncher;
    private Profile user;
    private ImageButton backButton;
    private ImageButton settingsButton;
    private ActivityResultLauncher<Intent> settingResultLauncher;
    private ActivityResultLauncher<Intent> profileResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login_screen);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("User")) {
            user = (Profile) intent.getSerializableExtra("User");
        } else {
            Log.d("AdminLoginActivity", "No User data found in Intent");
        }
        adminCodeInput = findViewById(R.id.edit_admin_code);

        // Links AdminLoginActivity to AdminActivity (main admin screen)
        adminMainResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );

        Button doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> checkAdminCode());

        settingResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );

        profileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                }
        );


        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLoginActivity.this, SettingsScreen.class);
                intent.putExtra("User", user);
                settingResultLauncher.launch(intent);
            }
        });

        ImageButton profileButton = findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLoginActivity.this, ProfileActivity.class);
                intent.putExtra("User", user);
                profileResultLauncher.launch(intent);
            }
        });

        settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLoginActivity.this, SettingsScreen.class);
                intent.putExtra("User", user);
                settingResultLauncher.launch(intent);
            }
        });

    }

    private void checkAdminCode() {
        String codeInput = adminCodeInput.getText().toString();

        // Check if the input is exactly 6 digits
        if (!TextUtils.isEmpty(codeInput) && codeInput.matches("\\d{6}")) {
            if (checkIfAdmin(codeInput)) {
                Toast.makeText(this, "Access granted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminLoginActivity.this, AdminActivity.class);
                intent.putExtra("User", user);
                adminMainResultLauncher.launch(intent);

            } else {
                Toast.makeText(this, "Incorrect admin code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a 6-digit number", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param password a string the user inputs to check if they're an admin
     * @return if the user is an admin or not
     */
    public boolean checkIfAdmin(String password){
        return password.equals(adminPassword);
    }
}
