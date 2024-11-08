package com.example.eventmaster;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests things in AdminLoginActivity
 */
@RunWith(AndroidJUnit4.class)
public class AdminLoginActivityTest {

    @Rule
    public ActivityScenarioRule<AdminLoginActivity> activityRule =
            new ActivityScenarioRule<>(AdminLoginActivity.class);

    private static final String CORRECT_ADMIN_CODE = "123456";
    private static final String INCORRECT_ADMIN_CODE = "000000";

    @Before
    public void setUp() {
        init();
    }

    @After
    public void tearDown() {
        release();
    }

    /**
     * Tests if inputting the correct code will send the user to the admin screen
     */
    @Test
    public void testAdminCorrectLogin() {
        ActivityScenario.launch(AdminLoginActivity.class);
        onView(withId(R.id.edit_admin_code)).perform(replaceText(CORRECT_ADMIN_CODE));
        onView(withId(R.id.done_button)).perform(click());
        intended(hasComponent(AdminActivity.class.getName()));
    }

    /**
     * Tests that inputting the incorrect code will not show the admin screen
     */
    @Test
    public void testAdminIncorrectLogin() {
        ActivityScenario.launch(AdminLoginActivity.class); // launch activity
        intended(hasComponent(AdminLoginActivity.class.getName())); // Verify AdminLoginActivity was launched

        onView(withId(R.id.edit_admin_code)).perform(replaceText(INCORRECT_ADMIN_CODE));
        onView(withId(R.id.done_button)).perform(click());
        Intents.assertNoUnverifiedIntents();

    }
}
