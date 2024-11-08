package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;
import org.junit.Test;

public class AdminActivityTest {

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    /**
     * Test to check if all views in AdminActivity are displayed
     */
    @Test
    public void testAllViewsVisible() {
        //properly displays all the data
        onView(withId(R.id.events_view)).check(matches(isDisplayed()));
        onView(withId(R.id.profiles_view)).check(matches(isDisplayed()));
        onView(withId(R.id.images_view)).check(matches(isDisplayed()));
        onView(withId(R.id.facilities_view)).check(matches(isDisplayed()));
        onView(withId(R.id.data_view)).check(matches(isDisplayed()));
    }
}
