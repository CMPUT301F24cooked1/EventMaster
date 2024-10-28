package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditFacilityScreen extends AppCompatActivity {

    private EditText editFacilityName;
    private EditText editFacilityAddress;
    private EditText editFacilityDesc;
    private AppCompatButton finishEditFacilityButton;
    private ImageButton backButton;
    private ImageButton profileButton;
    private ImageButton settingsButton;
    private ImageButton notificationButton;
    private ImageButton listButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_facility_screen);

        editFacilityName = findViewById(R.id.edit_facility_name);
        editFacilityAddress = findViewById(R.id.edit_facility_address);
        editFacilityDesc = findViewById(R.id.edit_facility_desc);

        Intent intentMain = getIntent();
        Profile user =  (Profile) intentMain.getSerializableExtra("FacilityEditUser");

        Facility userFacility = (Facility) getIntent().getSerializableExtra("FacilityEdit");

        editFacilityName.setText(userFacility.getFacilityName());
        editFacilityAddress.setText(userFacility.getFacilityAddress());
        editFacilityDesc.setText(userFacility.getFacilityDesc());

        //Return to FacilityScreen and save entered data.
        finishEditFacilityButton = findViewById(R.id.finish_edit_facility_button);
        finishEditFacilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if data entry is valid
                String newFacilityName = editFacilityName.getText().toString();
                String newFacilityAddress = editFacilityAddress.getText().toString();
                String newFacilityDesc = editFacilityDesc.getText().toString();
                boolean[] checkFacilityEntry = checkDataEntryFacility(newFacilityName, newFacilityAddress, newFacilityDesc);

                if (checkFacilityEntry[0]) {
                    if (checkFacilityEntry[1]) {
                        if (checkFacilityEntry[2]) {
                            //Saves edited values in Facility object and returns to FacilityScreen
                            userFacility.setFacilityName(newFacilityName);
                            userFacility.setFacilityAddress(newFacilityAddress);
                            userFacility.setFacilityDesc(newFacilityDesc);
                            Intent intent = new Intent(EditFacilityScreen.this, FacilityScreen.class);
                            intent.putExtra("editedUserFacility", userFacility);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(EditFacilityScreen.this, "Facility description must be under 100 characters.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditFacilityScreen.this, "Facility address must be under 50 characters.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditFacilityScreen.this, "Facility name must be under 30 characters.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Return to FacilityScreen and discard any entered data.
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Returns to previous screen
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        notificationButton = findViewById(R.id.notification_icon);

        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditFacilityScreen.this, Notifications.class);
            startActivity(intent);
        });

        settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditFacilityScreen.this, SettingsScreen.class);
            startActivity(intent);
        });

        profileButton = findViewById(R.id.profile);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditFacilityScreen.this, ProfileActivity.class);
            intent.putExtra("User", user);
            startActivity(intent);
        });

        listButton = findViewById(R.id.list_icon);

        listButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditFacilityScreen.this, ViewCreatedEventsActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Checks if the entries on the Edit Facility Screen are valid before saving them
     * @param facilityName the facility Name of the edited Facility
     * @param facilityAddress the facility Address of the edited Facility
     * @param facilityDesc the facility Description of the edited Facility
     */
    public boolean[] checkDataEntryFacility(String facilityName, String facilityAddress, String facilityDesc) {
        boolean[] entriesValid = {false, false, false};
        if (facilityName.length() <= 30 && !facilityName.isEmpty()) entriesValid[0] = true;
        if (facilityAddress.length() <= 50 && !facilityAddress.isEmpty()) entriesValid[1] = true;
        if (facilityDesc.length() <= 100 && !facilityDesc.isEmpty()) entriesValid[2] = true;
        return entriesValid;
    }
}