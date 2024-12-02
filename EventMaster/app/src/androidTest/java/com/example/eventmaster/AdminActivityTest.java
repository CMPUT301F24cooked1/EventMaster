package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the AdminActivityTest class
 */
@RunWith(AndroidJUnit4.class)
public class AdminActivityTest {
    private ActivityScenario<AdminActivity> scenario;
    private Profile mockUser;

    @Before
    public void setUp() {
        init();
        mockUser = new Profile("12345", "User", "user@example.com", "1234567890");

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.eventmaster", AdminActivity.class.getName());
        intent.putExtra("User", mockUser);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        release();
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Tests if event view is clickable and brings admin to the right screen
     */
    @Test
    public void testAdminEventScreen() {
        ActivityScenario.launch(AdminActivity.class);
        onView(withId(R.id.events_view)).perform(click());
        intended(hasComponent(AdminEventActivity.class.getName()));
    }

    /**
     * Tests if profile view is clickable and brings admin to the right screen
     */
    @Test
    public void testAdminProfileScreen() {
        ActivityScenario.launch(AdminActivity.class);
        onView(withId(R.id.profiles_view)).perform(click());
        intended(hasComponent(AdminProfileActivity.class.getName()));
    }

    /**
     * Tests if image view is clickable and brings admin to the right screen
     */
    @Test
    public void testAdminImagesScreen() {
        ActivityScenario.launch(AdminActivity.class);
        onView(withId(R.id.images_view)).perform(click());
        intended(hasComponent(AdminImagesActivity.class.getName()));
    }

    /**
     * Tests if facility view is clickable and brings admin to the right screen
     */
    @Test
    public void testAdminFacilitiesScreen() {
        ActivityScenario.launch(AdminActivity.class);
        onView(withId(R.id.facilities_view)).perform(click());
        intended(hasComponent(AdminFacilitiesActivity.class.getName()));
    }

    /**
     * Tests if qr view is clickable and brings admin to the right screen
     */
    @Test
    public void testAdminQRScreen() {
        ActivityScenario.launch(AdminActivity.class);
        onView(withId(R.id.data_view)).perform(click());
        intended(hasComponent(AdminQRActivity.class.getName()));
    }

    // test notifications button
    @Test
    public void testNavigationToNotifications() {
        onView(withId(R.id.nav_Notifications)).perform(click());
        intended(hasComponent(Notifications.class.getName()));
    }

    // test setting button
    @Test
    public void testNavigationToSettings() {
        onView(withId(R.id.nav_Settings)).perform(click());
        intended(hasComponent(SettingsScreen.class.getName()));
    }
    // test profile button
    @Test
    public void testNavigationToProfile() {
        onView(withId(R.id.nav_Profile)).perform(click());
        intended(hasComponent(ProfileActivity.class.getName()));
    }
    // test home button
    @Test
    public void testNavigationToHome() {
        onView(withId(R.id.nav_Home)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
    }

}
