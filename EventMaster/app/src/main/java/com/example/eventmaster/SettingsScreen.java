package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SettingsScreen extends AppCompatActivity {
    /**
     * Initializes the Setting Screen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * User can view and select all setting options
     */
    private Switch notificationSwitch;
    private ImageButton backButton;
    private ActivityResultLauncher<Intent> MainActivityResultLauncher;
    private ActivityResultLauncher<Intent> profileResultLauncher;
    private View adminPrivilegesButton;
    private ActivityResultLauncher<Intent> adminCodeResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_screen);

        Profile user = (Profile) getIntent().getSerializableExtra("User"); // user from MainActivity

        notificationSwitch = findViewById(R.id.notification_button);

        // Toggle on and off the notification button
        notificationSwitch.setChecked(true); // enabled notifications
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean toggleNotifications) {
                if (toggleNotifications){
                    user.setNotifications(true);  // set notifications to on
                //    Toast.makeText(SettingsScreen.this, "Notifications ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    user.setNotifications(false);
                //    Toast.makeText(SettingsScreen.this, "Notifications OFF", Toast.LENGTH_SHORT).show();
                }

            }
        });

        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to MainActivity class
                Intent intent = new Intent(SettingsScreen.this, MainActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
            }
        });

        ImageButton profileButton = findViewById(R.id.profile);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to ProfileActivity class
                Intent intent = new Intent(SettingsScreen.this, ProfileActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
            }
        });


        //Links the Settings Screen to the Admin Login screen
        adminCodeResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                    }
                }
        );
        adminPrivilegesButton = findViewById(R.id.admin_privileges_view);
        adminPrivilegesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send user to SettingsScreen class
                Log.d("SettingsScreen", "Admin privileges button clicked");

                Intent intent = new Intent(SettingsScreen.this, AdminLoginActivity.class);
                intent.putExtra("User", user);
                adminCodeResultLauncher.launch(intent);
            }
        });

    }


}
