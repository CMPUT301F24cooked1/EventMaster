package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
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
 * Tests the SettingsScreen class (settings screen)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsTest {

    @Rule
    public ActivityScenarioRule<SettingsScreen> activityRule = new ActivityScenarioRule<>(
            new Intent(ApplicationProvider.getApplicationContext(), SettingsScreen.class)
                    .putExtra("User", new Profile("12345", "User", "user@example.com", "123456789"))
    );

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
     * Tests Nav Bar on the settings screen
     */
    @Test
    public void testNavigationBarButtons() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsScreen.class);
        Profile testUser = new Profile("12345", "User", "user@example.com", "123456789");
        intent.putExtra("User", testUser);
        try (ActivityScenario<SettingsScreen> scenario = ActivityScenario.launch(intent)) {

            onView(withId(R.id.nav_Home)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_Settings)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_Notifications)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_Profile)).check(matches(isDisplayed()));

            onView(withId(R.id.nav_Home)).perform(click());
            handleNotificationPermissionPopUp();

            onView(withId(R.id.nav_Notifications)).perform(click());

            onView(withId(R.id.nav_Profile)).perform(click());
        }
    }

    /**
     * Tests Dark mode Switch
     */
    @Test
    public void testDarkModeSwitchToggle() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsScreen.class);
        Profile testUser = new Profile("12345", "User", "user@example.com", "123456789");
        intent.putExtra("User", testUser);
        try (ActivityScenario<SettingsScreen> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.mode_switch)).check(matches(isNotChecked()));

            onView(withId(R.id.mode_switch)).perform(click());
            onView(withId(R.id.mode_switch)).check(matches(isChecked()));

            onView(withId(R.id.mode_switch)).perform(click());
            onView(withId(R.id.mode_switch)).check(matches(isNotChecked()));
        }
    }

    /**
     * Tests Admin login screen
     */
    @Test
    public void testAdminPrivilegesButton() {
        Intents.init();

        try {
            Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsScreen.class);
            Profile testUser = new Profile("12345", "User", "user@example.com", "123456789");
            intent.putExtra("User", testUser);

            try (ActivityScenario<SettingsScreen> scenario = ActivityScenario.launch(intent)) {
                onView(withId(R.id.admin_privileges_view)).perform(click());

                intended(hasComponent(AdminLoginActivity.class.getName()));
            }
        } finally {
            Intents.release();
        }
    }

    /**
     * Tests App info screen
     */
    @Test
    public void testAppInfoButton() {
        Intents.init();

        try {
            Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsScreen.class);
            Profile testUser = new Profile("12345", "User", "user@example.com", "123456789");
            intent.putExtra("User", testUser);

            try (ActivityScenario<SettingsScreen> scenario = ActivityScenario.launch(intent)) {
                onView(withId(R.id.app_info)).perform(click());

                intended(hasComponent(AppInfoActivity.class.getName()));
            }
        } finally {
            Intents.release();
        }
    }




}
