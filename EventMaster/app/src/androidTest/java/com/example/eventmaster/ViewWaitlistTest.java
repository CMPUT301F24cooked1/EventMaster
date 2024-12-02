package com.example.eventmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs UI tests on the ViewWaitlistActivity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewWaitlistTest {
    @Rule
    public ActivityScenarioRule<ViewWaitlistActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), ViewWaitlistActivity.class)
                    .putExtra("User", new Profile("UiTestID26245408", "Autechre", "Autechre@gmail.com", "1112223333"))
                    .putExtra("eventName", "Autechre Live Show"));

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
    public void testWaitlistDisplayed() throws InterruptedException {
        Thread.sleep(3000);
        onView(withId(R.id.waitlistRecyclerView)).check(new RecyclerViewItemCountAssertion(2));
    }

    /**
     * Test to check if sample button will correctly not sample more entrants when the limit has been reached
     */
    @Test
    public void testSamplingButton() throws InterruptedException {
        Thread.sleep(3000);
        onView(withId(R.id.waitlistRecyclerView)).check(new RecyclerViewItemCountAssertion(2));
        onView(withId(R.id.choose_sample_button)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.waitlistRecyclerView)).check(new RecyclerViewItemCountAssertion(3));
    }

    /**
     * Test if Event will sample correct number of new users.
     */
    @Test
    public void testSamplingButton2() throws InterruptedException {

        FirebaseFirestore firestore;
        firestore = FirebaseFirestore.getInstance();

        Map<String, Object> data1 = new HashMap<>();
        data1.put("eventCapacity", 5);
        firestore.collection("facilities")
                .document("UiTestID26245408")
                .collection("My Events")
                .document("Autechre Live Show")
                .set(data1, SetOptions.merge()) // Merge with existing data
                .addOnSuccessListener(aVoid -> {

                });
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onView(withId(R.id.waitlistRecyclerView)).check(new RecyclerViewItemCountAssertion(2));
        onView(withId(R.id.choose_sample_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onView(withId(R.id.waitlistRecyclerView)).check(new RecyclerViewItemCountAssertion(5));

        Map<String, Object> data2 = new HashMap<>();
        data2.put("eventCapacity", 3);
        data2.put("selectedCount", 3);

        firestore.collection("facilities")
                .document("UiTestID26245408")
                .collection("My Events")
                .document("Autechre Live Show")
                .set(data2, SetOptions.merge()) // Merge with existing data
                .addOnSuccessListener(bVoid -> {
                    Log.d("UITest", "success");
                });

        Map<String, Object> data3 = new HashMap<>();
        data3.put("eventId", "Autechre Live Show");
        firestore.collection("facilities")
                .document("UiTestID26245408")
                .collection("My Events")
                .document("Autechre Live Show")
                .collection("unsampled list")
                .document("testEntrant2")
                .set(data3, SetOptions.merge())
                .addOnSuccessListener(bVoid -> {
                    Log.d("Firestore", "Device ID field updated in invited list successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating device ID field in invited list", e);
                });

        Map<String, Object> data4 = new HashMap<>();
        data4.put("eventId", "Autechre Live Show");
        firestore.collection("facilities")
                .document("UiTestID26245408")
                .collection("My Events")
                .document("Autechre Live Show")
                .collection("unsampled list")
                .document("testEntrant4")
                .set(data4, SetOptions.merge())
                .addOnSuccessListener(bVoid -> {
                    Log.d("Firestore", "Device ID field updated in invited list successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating device ID field in invited list", e);
                });

        firestore.collection("facilities")
                .document("UiTestID26245408")
                .collection("My Events")
                .document("Autechre Live Show")
                .collection("invited list")
                .document("testEntrant2")
                .delete()
                .addOnSuccessListener(bVoid -> {
                    Log.d("Firestore", "Device ID field updated in unsampled list successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating device ID field in unsampled list", e);
                });

        firestore.collection("facilities")
                .document("UiTestID26245408")
                .collection("My Events")
                .document("Autechre Live Show")
                .collection("invited list")
                .document("testEntrant4")
                .delete()
                .addOnSuccessListener(bVoid -> {
                    Log.d("Firestore", "Device ID field updated in unsampled list successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating device ID field in unsampled list", e);
                });
    }
}
