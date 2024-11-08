package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FacilityScreenTest {
    @Rule
    public ActivityScenarioRule<FacilityScreen> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), FacilityScreen.class)
                    .putExtra("User", new Profile("UiTestID14485647", "Roland Rahsaan Kirk", "RolandRKirk@gmail.com", "2345678989"))
                    .putExtra("Facility", new Facility("UiTestID14485647", "Kirk's Facility", "Unknown", "...")));

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Test to check if the activity grabs the intent and displays the correct information.
     */
    @Test
    public void testCorrectTextDisplayed() {
        onView(withId(R.id.facilityNameText)).check(matches(withText("Kirk's Facility")));
        onView(withId(R.id.facilityAddressText)).check(matches(withText("Unknown")));
        onView(withId(R.id.facilityDescText)).check(matches(withText("...")));
    }

    /**
     * Test to check if Editing the Facility updates the Text on FacilityScreen
     */
    @Test
    public void testEditFacility() {
        //Press edit facility button and check if intent was sent to EditFacilityScreen.
        onView(withId(R.id.edit_facility_button)).perform(click());
        intended(hasComponent(EditFacilityScreen.class.getName()));

        //Edit text fields and press finish button.
        onView(withId(R.id.edit_facility_name)).perform(typeText(" Up"), closeSoftKeyboard());
        onView(withId(R.id.edit_facility_address)).perform(typeText(" Up"), closeSoftKeyboard());
        onView(withId(R.id.edit_facility_desc)).perform(typeText(" This is updated."), closeSoftKeyboard());
        onView(withId(R.id.finish_edit_facility_button)).perform(click());

        //Check if values were updated.
        onView(withId(R.id.facilityNameText)).check(matches(withText("Kirk's Facility Up")));
        onView(withId(R.id.facilityAddressText)).check(matches(withText("Unknown Up")));
        onView(withId(R.id.facilityDescText)).check(matches(withText("... This is updated.")));
    }

    /**
     * Test to check if character limits for Facility are working.
     */
    @Test
    public void testEditFacilityConstraints() {
        //Press edit facility button and check if intent was sent to EditFacilityScreen.
        onView(withId(R.id.edit_facility_button)).perform(click());
        intended(hasComponent(EditFacilityScreen.class.getName()));

        //Edit Facility name and press finish button.
        onView(withId(R.id.edit_facility_name)).perform(typeText(" 51 Characters: ////////////////////"), closeSoftKeyboard());
        onView(withId(R.id.finish_edit_facility_button)).perform(click());

        //Check if views in previous activity exist to see if the action was cancelled correctly
        onView(withId(R.id.facilityNameText)).check(doesNotExist());
        onView(withId(R.id.facilityAddressText)).check(doesNotExist());
        onView(withId(R.id.facilityDescText)).check(doesNotExist());
    }
}
