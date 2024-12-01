package com.example.eventmaster;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static java.util.regex.Pattern.matches;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the MainActivity class (home screen)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

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
     * Tests the settings button
     */
    @Test
    public void testSettings() {
        handleNotificationPermissionPopUp();
        onView(withId(R.id.nav_Settings)).perform(click());
        onView(withId(R.id.app_info_text)).check(matches(isDisplayed()));
    }

    /**
     * Tests the join event button
     */
    @Test
    public void testJoinEventButton() {
        handleNotificationPermissionPopUp();
        onView(withId(R.id.join_event_button)).perform(click());
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

}
