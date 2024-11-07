package com.example.eventmaster;

import android.content.pm.ActivityInfo;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QrCaptureActivity extends CaptureActivity {

    /**
     * Ensures the orientation of the QR Scanner is vertical rather than horizontal
     */
    @Override
    public void onResume() {
        super.onResume();  // ensure the scanner is vertical
    }

}
