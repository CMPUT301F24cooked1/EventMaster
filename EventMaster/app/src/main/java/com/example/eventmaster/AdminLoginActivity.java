package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
