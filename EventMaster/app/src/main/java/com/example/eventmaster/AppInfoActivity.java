package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays various pieces of information about the EventMaster app.
 */
public class AppInfoActivity extends AppCompatActivity {
    private ImageButton backButton;
    private ImageButton settingsButton;
    private ActivityResultLauncher<Intent> settingResultLauncher;
    private ActivityResultLauncher<Intent> profileResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(this);
        setContentView(R.layout.app_info_screen);

        Profile user = (Profile) getIntent().getSerializableExtra("User");

        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

}
