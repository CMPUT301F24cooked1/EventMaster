package com.example.eventmaster;

import android.view.WindowManager;
import androidx.test.espresso.Root;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom matcher to verify if a Toast message is displayed.
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is a Toast");
    }

    @Override
    protected boolean matchesSafely(Root root) {
        // Check that the root's window type corresponds to a Toast
        try {
            // Using reflection to access the type field of WindowLayoutParams
            Object params = root.getWindowLayoutParams();
            if (params != null) {
                // Check if it's a Toast by accessing the WindowManager.LayoutParams 'type' field dynamically
                Integer type = (Integer) params.getClass().getDeclaredField("type").get(params);
                return type != null && type == WindowManager.LayoutParams.TYPE_TOAST;
            }
        } catch (Exception e) {
            // If reflection fails, return false
            e.printStackTrace();
        }
        return false;
    }
}
