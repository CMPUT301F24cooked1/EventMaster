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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.intent.Intents;  // Import Intents
import androidx.test.espresso.action.ViewActions; // Import for pressBack()

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void testCreateEventWithValidData() throws InterruptedException {
        String eventName = "Sample Event121";

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

    private boolean checkIfEventExists(String eventName) throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicBoolean exists = new AtomicBoolean(false);

        // Initialize a CountDownLatch for the main query completion
        CountDownLatch latch = new CountDownLatch(1);

        // Query all documents in the "facilities" collection
        db.collection("facilities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Get the list of facility documents
                        QuerySnapshot facilityDocs = task.getResult();

                        // Track how many sub-queries are complete
                        AtomicInteger completedFacilityChecks = new AtomicInteger(0);

                        for (DocumentSnapshot facilityDoc : facilityDocs) {
                            // Query each facility's "My Events" subcollection
                            facilityDoc.getReference().collection("My Events")
                                    .whereEqualTo("eventName", eventName)
                                    .get()
                                    .addOnCompleteListener(eventTask -> {
                                        if (eventTask.isSuccessful() && !eventTask.getResult().isEmpty()) {
                                            exists.set(true);  // Event exists, set flag
                                            latch.countDown(); // Release the latch immediately
                                        }

                                        // Increment the count of completed checks
                                        if (completedFacilityChecks.incrementAndGet() == facilityDocs.size()) {
                                            // Only count down if all sub-queries are done and no event was found
                                            if (!exists.get()) {
                                                latch.countDown();
                                            }
                                        }
                                    });
                        }
                    } else {
                        latch.countDown(); // If main query fails, release the latch
                    }
                });

        latch.await();  // Wait for the entire process to complete

        return exists.get();
    }
}