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
     * Tests the settings button
     */
    @Test
    public void testSettings() {
        onView(withId(R.id.settings)).perform(click());
        onView(withId(R.id.app_info_text)).check(matches(isDisplayed()));
    }

    /**
     * Tests the join event button
     */
    @Test
    public void testJoinEventButton() {
        onView(withId(R.id.join_event_button)).perform(click());
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Tests the profile button
     */
    @Test
    public void testProfileButton() {
        onView(withId(R.id.profile)).perform(click());
        onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
    }

    /**
     * Tests the create event button
     */
    @Test
    public void testCreateEventButton() {
        onView(withId(R.id.create_event_button)).perform(click());
        onView(withId(R.id.facilityNameText)).check(matches(isDisplayed()));
    }
}
