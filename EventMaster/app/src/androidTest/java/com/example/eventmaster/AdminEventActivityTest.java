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

import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs tests on AdminEventActivity class
 */
public class AdminEventActivityTest {
    private ActivityScenario<AdminEventActivity> scenario;
    private Profile mockUser;

    @Before
    public void setUp() {
        init();
        mockUser = new Profile("12345", "User", "user@example.com", "1234567890");

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.eventmaster", AdminEventActivity.class.getName());
        intent.putExtra("User", mockUser);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        release();
        if (scenario != null) {
            scenario.close();
        }
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
