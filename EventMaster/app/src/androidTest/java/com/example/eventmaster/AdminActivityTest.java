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

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the AdminActivityTest class
 */
public class AdminActivityTest {

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Before
    public void setUp() {
        init();
    }

    @After
    public void tearDown() {
        release();
    }

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

}
