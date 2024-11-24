package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Gives the user EditText views to edit their personal Facility.
 */
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
    private ActivityResultLauncher<Intent> ProfileActivityResultLauncher;
    private ActivityResultLauncher<Intent> notificationActivityResultLauncher;
    private ActivityResultLauncher<Intent> settingsResultLauncher;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private Profile user;

    /**
     * @param savedInstanceState If the activity is restarted this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_facility_screen);

        editFacilityName = findViewById(R.id.edit_facility_name);
        editFacilityAddress = findViewById(R.id.edit_facility_address);
        editFacilityDesc = findViewById(R.id.edit_facility_desc);

        Intent intentMain = getIntent();
        user =  (Profile) intentMain.getSerializableExtra("FacilityEditUser");

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

        ImageButton backButton = findViewById(R.id.back);
        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        settingsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        MainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        notificationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });

        ProfileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Profile updatedUser = (Profile) result.getData().getSerializableExtra("User");
                        if (updatedUser != null) {
                            user = updatedUser; // Apply the updated Profile to MainActivity's user
                            Log.d("MainActivity", "User profile updated: " + user.getName());
                        }
                    }

                });
    }

    /**
     * Checks if the entries on the Edit Facility Screen are valid before saving them
     * @param facilityName the facility Name of the edited Facility
     * @param facilityAddress the facility Address of the edited Facility
     * @param facilityDesc the facility Description of the edited Facility
     * @return boolean array for each entry stating if it is valid or not
     */
    public boolean[] checkDataEntryFacility(String facilityName, String facilityAddress, String facilityDesc) {
        boolean[] entriesValid = {false, false, false};
        if (facilityName.length() <= 50 && !facilityName.isEmpty()) entriesValid[0] = true;
        if (facilityAddress.length() <= 60 && !facilityAddress.isEmpty()) entriesValid[1] = true;
        if (facilityDesc.length() <= 500 && !facilityDesc.isEmpty()) entriesValid[2] = true;
        return entriesValid;
    }
}