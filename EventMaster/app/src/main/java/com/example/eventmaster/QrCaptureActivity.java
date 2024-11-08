package com.example.eventmaster;

import android.content.pm.ActivityInfo;
import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * The QrCaptureActivity class customizes the barcode scanning activity
 * to ensure the scanner is always displayed in a vertical orientation.
 * <p>
 * This class extends {@link com.journeyapps.barcodescanner.CaptureActivity} and overrides
 * the default behavior to lock the screen orientation to portrait.
 * </p>
 */
public class QrCaptureActivity extends CaptureActivity {

    /**
     * Ensures the orientation of the QR Scanner is vertical rather than horizontal
     */
    @Override
    public void onResume() {
        super.onResume();  // ensure the scanner is vertical
    }

}
