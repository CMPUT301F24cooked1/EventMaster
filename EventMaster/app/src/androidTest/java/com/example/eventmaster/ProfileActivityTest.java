package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the ProfileActivity class (profile screen)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileActivityTest {

    @Test
    public void testProfileDisplay() {
        // Launch the activity with an intent carrying a test profile
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ProfileActivity.class);
        Profile user = new Profile("12345", "Name", "name@gmail.com", "123456789");
        intent.putExtra("User", user);
        ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(intent);

        // Checks for data that we should be able to see
        onView(withId(R.id.profile_name)).check(matches(withText("Name")));
        onView(withId(R.id.profile_email)).check(matches(withText("name@gmail.com")));
        onView(withId(R.id.profile_phone_number)).check(matches(withText("123456789")));

        // Check that the profile picture is displayed
        onView(withId(R.id.profile_picture)).check(matches(isDisplayed()));

        // Check that the Edit Profile button is displayed and perform a click action
        onView(withId(R.id.edit_profile_button)).check(matches(isDisplayed())).perform(click());


    }
}
