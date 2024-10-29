package com.example.eventmaster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScanFragment extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_qr_screen);

        AppCompatButton scanButton = findViewById(R.id.scan_qr_code_button);

        // click on the scan button
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // implement scanner inside here
                ScanCode();

            }
        });

    }

    private void ScanCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(false);
        options.setOrientationLocked(false);
        options.setCaptureActivity(CaptureActivity.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null)
        {
            // information from ViewEventsAdapter
            String scannedHash = result.getContents();
            Intent intent = getIntent();
            String event = intent.getStringExtra("event");
            String deviceID = intent.getStringExtra("deviceID");  // facility device id

            // send information over to retrieveEventInfo
            Intent intent2 = new Intent(QRScanFragment.this, retrieveEventInfo.class);
            intent2.putExtra("HASHED_DATA", scannedHash);
            intent2.putExtra("event", event);
            intent2.putExtra("deviceID", deviceID);  // facility device id

            startActivity(intent2);
        }
    });







}


