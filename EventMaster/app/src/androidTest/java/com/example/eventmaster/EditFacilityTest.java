package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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
public class EditFacilityTest {
    @Rule
    public ActivityScenarioRule<EditFacilityScreen> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), EditFacilityScreen.class)
                    .putExtra("FacilityEditUser", new Profile("UiTestID", "Paul Buchanan", "PaulBuchanan@gmail.com", "01415671111"))
                    .putExtra("FacilityEdit", new Facility("UiTestID", "Tinseltown", "In the Rain", "Easy come and easy go.")));

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
        onView(withId(R.id.edit_facility_name)).check(matches(withText("Tinseltown")));
        onView(withId(R.id.edit_facility_address)).check(matches(withText("In the Rain")));
        onView(withId(R.id.edit_facility_desc)).check(matches(withText("Easy come and easy go.")));
    }

}