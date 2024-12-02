package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
public class ViewInvitedListTest {
    @Rule
    public ActivityScenarioRule<ViewInvitedListActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), ViewInvitedListActivity.class)
                    .putExtra("User", new Profile("UiTestID26245408", "Autechre", "Autechre@gmail.com", "1112223333"))
                    .putExtra("Sampled?", 0)
                    .putExtra("myEventName", "Autechre Live Show"));

    @Before
    public void setUp() {
        // Initialize intents before each test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release intents after each test
        Intents.release();
    }

    /**
     * Test to check if the activity grabs and displays the correct number of entrants from firestore
     */
    @Test
    public void testInvitedListDisplayed() throws InterruptedException {
        Thread.sleep(3000);
        onView(withId(R.id.waitlistRecyclerView)).check(new RecyclerViewItemCountAssertion(3));
    }


}
