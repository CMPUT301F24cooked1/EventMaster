package com.example.eventmaster;

import android.content.pm.ActivityInfo;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QrCaptureActivity extends CaptureActivity {
    @Override
    public void onResume() {
        super.onResume();  // ensure the scanner is vertical
    }

}
