package com.example.eventmaster;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import android.view.View;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

//https://stackoverflow.com/questions/36399787/how-to-count-recyclerview-items-with-espresso, 2024-12-02

/**
 * This class is used in UI tests to assert if recycler views contain the correct number of items
 */
public class RecyclerViewItemCountAssertion implements ViewAssertion {
    private final int expectedCount;

    /**
     * Constructs a RecyclerViewItemCountAssertion object
     * @param expectedCount the expected number of items in the recycler view
     */
    public RecyclerViewItemCountAssertion(int expectedCount) {
        this.expectedCount = expectedCount;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        assertThat(adapter.getItemCount(), is(expectedCount));
    }
}
