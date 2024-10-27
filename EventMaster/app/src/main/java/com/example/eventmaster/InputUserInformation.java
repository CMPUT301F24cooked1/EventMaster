package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class InputUserInformation extends AppCompatActivity {
    private EditText name_edit;
    private EditText email_edit;
    private EditText password_edit;
    private EditText phone_number_edit;
    private Button profile_change_button;
    private ImageButton back_button;
    private ActivityResultLauncher<Intent> profileActivityResultLauncher;


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

        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        name_edit = findViewById(R.id.edit_name);
        email_edit = findViewById(R.id.edit_email);
        phone_number_edit = findViewById(R.id.edit_phone_number);
        profile_change_button = findViewById(R.id.save_changes_button);
        back_button = findViewById(R.id.back);

        profile_change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_edit.getText().toString();
                String email = email_edit.getText().toString();
                String phone_number = phone_number_edit.getText().toString();
                user.setName(name);
                user.setEmail(email);
                user.setPhone_number(phone_number);
            }
        });

        profileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result ->{
                    if (result.getResultCode() == RESULT_OK){

                    }
                }
        );
        
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InputUserInformation.this, ProfileActivity.class);
                intent.putExtra("User", user);
                profileActivityResultLauncher.launch(intent);
            }
        });
    }
}
