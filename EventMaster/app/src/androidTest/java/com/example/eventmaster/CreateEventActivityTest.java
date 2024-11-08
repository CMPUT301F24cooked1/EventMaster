package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventmaster.CreateEventActivity;
import com.example.eventmaster.ToastMatcher;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.intent.Intents;  // Import Intents
import androidx.test.espresso.action.ViewActions; // Import for pressBack()

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventActivityTest {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), CreateEventActivity.class)
                    .putExtra("User", new Profile("UiTestID14485647", "Roland Rahsaan Kirk", "RolandRKirk@gmail.com", "2345678989")));

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
     * Test to verify all the fields and buttons are displayed on the screen
     */
    @Test
    public void testBasicUIDisplay() {
        activityRule.getScenario().recreate();
        // Ensure that we're starting from the CreateEventActivity screen
        onView(withId(R.id.eventName)).check(matches(isDisplayed()));
        onView(withId(R.id.eventDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.eventCapacity)).check(matches(isDisplayed()));
        onView(withId(R.id.waitlistCountdown)).check(matches(isDisplayed()));
        onView(withId(R.id.createEventButton)).check(matches(isDisplayed()));
    }

    /**
     * Test to verify that entering valid data and clicking the create event button creates the event and shows success Toast
     */
    @Test
    public void testCreateEventWithValidData() {
        String eventName = "Sample Event12";

        // Query the database to check if the event name already exists.
        boolean eventExists = checkIfEventExists(eventName);
        if (eventExists) {
            System.out.println("Event with name " + eventName + " already exists. Skipping test.");
            return; // Skip the test if the event name exists
        }

        // Enter event details with valid data
        onView(withId(R.id.eventName)).perform(typeText(eventName), closeSoftKeyboard());
        onView(withId(R.id.eventDescription)).perform(typeText("Event Description"), closeSoftKeyboard());
        onView(withId(R.id.eventCapacity)).perform(typeText("100"), closeSoftKeyboard());
        // Clear the background text in the waitlist countdown field before interacting with it
        onView(withId(R.id.waitlistCountdown)).perform(ViewActions.clearText());
        onView(withId(R.id.waitlistCountdown)).perform(typeText("2025-05-10 12:00:00"), closeSoftKeyboard());

        // Create the event
        onView(withId(R.id.createEventButton)).perform(click());

        // Verify the Toast message or other UI elements
        onView(withText("Event created successfully"))
                .inRoot(new ToastMatcher()) // Assuming ToastMatcher is set up correctly
                .check(matches(isDisplayed()));
    }

    private boolean checkIfEventExists(String eventName) {
        boolean[] exists = new boolean[1];  // Use an array to modify value in async callback
        String deviceId = Settings.Secure.getString(ApplicationProvider.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get the Firestore instance and query the correct path
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facilities")
                .document(deviceId)  // Target the specific facility document
                .collection("My Events")       // Query under the "My Events" collection
                .whereEqualTo("eventName", eventName)  // Check if eventName exists
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        exists[0] = true;  // Event exists, set to true
                    } else {
                        exists[0] = false;  // Event does not exist, set to false
                    }
                });

        // Wait for async completion
        try {
            Thread.sleep(2000); // You can use CountDownLatch or other synchronization here
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return exists[0];
    }
}
