package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Matcher;
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
        TestUtils.disableAnimations();
    }

    @After
    public void tearDown() {
        release();
        TestUtils.enableAnimations();
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

    /**
     * Tests if the delete button works properly
     */
    @Test
    public void testDeleteButton(){
        ActivityScenario.launch(AdminEventActivity.class); // launch the activity
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed())); //proper recycler view is being displayed

        onView(withId(R.id.delete_button)).perform(click()); // checks that the delete button can be clicked
        onView(withId(R.id.delete_button)).perform(click()); // checks that the delete button can be restored to its original state

    }
}
