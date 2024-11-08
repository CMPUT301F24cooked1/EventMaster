package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.widget.Switch;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the SettingsScreen class (settings screen)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsTest {

    @Rule
    public ActivityScenarioRule<SettingsScreen> activityRule = new ActivityScenarioRule<>(SettingsScreen.class);

    /**
     * Launches the activity with a Profile object
     */
    private void launchSettingsScreen(boolean initialNotificationState) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsScreen.class);
        Profile testUser = new Profile("12345", "User", "user@example.com", "123456789");
        testUser.setNotifications(initialNotificationState);
        intent.putExtra("User", testUser);
        activityRule.getScenario().onActivity(activity -> activity.startActivity(intent));
    }

    /**
     * Tests some UI components on setting screen
     */
    @Test
    public void testUIComponents() {
        launchSettingsScreen(false);
        onView(withId(R.id.notification_button)).check(matches(isDisplayed()));
        onView(withId(R.id.back)).check(matches(isDisplayed()));
        onView(withId(R.id.profile)).check(matches(isDisplayed()));
    }

    /**
     * Tests the back button navigation to MainActivity.
     */
    @Test
    public void testBackButton() {
        launchSettingsScreen(false);
        onView(withId(R.id.back)).perform(click());
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }

}
