package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Runs tests on AdminEventActivity class
 */
public class AdminEventActivityTest {

    @Rule
    public ActivityScenarioRule<AdminEventActivity> activityRule =
            new ActivityScenarioRule<>(AdminEventActivity.class);

    @Before
    public void setUp() {
        init();
    }

    @After
    public void tearDown() {
        release();
    }

    /**
     * Tests if the recycler view properly displays the events
     */
    @Test
    public void testEventListDisplayed() {
        ActivityScenario.launch(AdminEventActivity.class); // launch the activity

        // Verify RecyclerView is displayed
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }
}
