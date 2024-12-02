package com.example.eventmaster;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.eventmaster.JoinEventScreen;
import com.example.eventmaster.Profile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.action.ViewActions.click;

/**
 * Tests for the JoinedEventsActivity class
 */
@RunWith(AndroidJUnit4.class)
public class WaitlistedEventsTest {

    private ActivityScenario<JoinEventScreen> scenario;
    private Profile mockUser;

    @Before
    public void setUp() {
        Intents.init();

        mockUser = new Profile("12345", "User", "user@example.com", "1234567890");

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.eventmaster", WaitlistedEventsActivity.class.getName());
        intent.putExtra("User", mockUser);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        Intents.release();
        if (scenario != null) {
            scenario.close();
        }
    }
    // test recycle view
    @Test
    public void testRecyclerViewDisplaysEvents() {
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }


    // test notifications button
    @Test
    public void testNavigationToNotifications() {
        onView(withId(R.id.nav_Notifications)).perform(click());
        intended(hasComponent(Notifications.class.getName()));
    }

    // test setting button
    @Test
    public void testNavigationToSettings() {
        onView(withId(R.id.nav_Settings)).perform(click());
        intended(hasComponent(SettingsScreen.class.getName()));
    }
    // test profile button
    @Test
    public void testNavigationToProfile() {
        onView(withId(R.id.nav_Profile)).perform(click());
        intended(hasComponent(ProfileActivity.class.getName()));
    }
    // test home button
    @Test
    public void testNavigationToHome() {
        onView(withId(R.id.nav_Home)).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
    }

}