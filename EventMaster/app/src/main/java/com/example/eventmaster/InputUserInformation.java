package com.example.eventmaster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class InputUserInformation extends AppCompatActivity {
    private EditText name_edit;
    private EditText email_edit;
    private EditText password_edit;
    private EditText phone_number_edit;
    private Button profile_change_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_screen);

        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        name_edit = findViewById(R.id.edit_name);
        email_edit = findViewById(R.id.edit_email);
        phone_number_edit = findViewById(R.id.edit_phone_number);
        profile_change_button = findViewById(R.id.edit_profile_button);

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
    }
}
