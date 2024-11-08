package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the InputUserInformation class (edit profile screen)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditProfileTest {

    @Rule
    public ActivityScenarioRule<InputUserInformation> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), InputUserInformation.class)
                    .putExtra("User", new Profile("12345", "Johnny", "johnny@gmail.com", "23475242")));

    @Before
    public void setUp() {
        init();
    }

    @After
    public void tearDown() {
        release();
    }

    /**
     * Test to verify all the data shows up
     */
    @Test
    public void testBasicUIDisplay() {
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_phone_number)).check(matches(isDisplayed()));
        onView(withId(R.id.save_changes_button)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_picture)).check(matches(isDisplayed()));
        onView(withId(R.id.upload_profile_picture)).check(matches(isDisplayed()));
    }

    /**
     * Test to verify that entering valid data and clicking the save button updates user information.
     */
    @Test
    public void testProfileActivityDisplaysUpdatedInfo() {
        Profile updatedUser = new Profile("12345", "Updated Name", "updated@gmail.com", "987654321");

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ProfileActivity.class);
        intent.putExtra("User", updatedUser);
        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(intent)) {


            onView(withId(R.id.profile_name)).check(matches(withText("Updated Name")));
            onView(withId(R.id.profile_email)).check(matches(withText("updated@gmail.com")));
            onView(withId(R.id.profile_phone_number)).check(matches(withText("987654321")));
        }
    }



}
