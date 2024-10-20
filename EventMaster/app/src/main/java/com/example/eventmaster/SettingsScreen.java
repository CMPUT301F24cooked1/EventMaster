package com.example.eventmaster;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsScreen extends AppCompatActivity {
    private Switch notificationSwitch;
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


    }


}
