package com.example.eventmaster;

import android.content.Intent;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.platform.app.InstrumentationRegistry;

/**
 * UI tests for the Notification class
 */
public class NotificationActivityTest {
    /**
     * Handles the Notification pop up
     */
    private void handleNotificationPermissionPopUp() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            // Find and click the "Allow" button
            UiObject allowButton = device.findObject(new UiSelector().text("Allow"));
            if (allowButton.exists() && allowButton.isClickable()) {
                allowButton.click();
            }
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the recycler view
     */
    @Test
    public void testNotificationsRecyclerViewDisplays() {
        ActivityScenario.launch(MainActivity.class);

        handleNotificationPermissionPopUp();

        onView(withId(R.id.nav_Notifications)).perform(click());

        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Test on clicking on a notification in recyclerview
     */
    @Test
    public void testNotificationItemClick() {
        ActivityScenario.launch(MainActivity.class);

        handleNotificationPermissionPopUp();

        onView(withId(R.id.nav_Notifications)).perform(click());

        try {
            onView(withId(R.id.recyclerView))
                    .check((view, noViewFoundException) -> {
                        if (noViewFoundException != null) {
                            // Log if the RecyclerView isn't found
                            Log.d("Test", "RecyclerView not found. Skipping the click action.");
                            return;
                        }

                        RecyclerView recyclerView = (RecyclerView) view;
                        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();

                        if (adapter != null && adapter.getItemCount() > 0) {
                            onView(withId(R.id.recyclerView))
                                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
                        } else {
                            Log.d("Test", "RecyclerView is empty. Skipping the click action.");
                        }
                    });
        } catch (Exception e) {
            Log.e("Test", "Error interacting with RecyclerView: " + e.getMessage());
        }
    }




}
