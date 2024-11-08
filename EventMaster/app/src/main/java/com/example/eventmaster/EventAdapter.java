package com.example.eventmaster;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.units.qual.A;

import java.util.List;

import kotlin.contracts.Returns;

/**
 * Adapter class for managing and binding event data to RecyclerView.
 * Displays a list of events, and each item can be clicked to navigate
 * to OrganizerEventListView, passing relevant data such as user profile.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;
    private Profile user; // Add a Profile member variable

    /**
     * Constructs an EventAdapter with the specified list of events, context, and user profile.
     *
     * @param eventList List of events to display
     * @param context Context for inflating layouts and launching new activities
     * @param user User profile to pass when an event is clicked
     */
    public EventAdapter(List<Event> eventList, Context context, Profile user) {
        this.eventList = eventList;
        this.context = context;
        this.user = user; // Store the user object
    }


    /**
     * Called when RecyclerView needs a new {@link EventViewHolder} of the given type
     * to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new EventViewHolder that holds a View for each event
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }
    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder to be updated with event data
     * @param position The position of the item within the data set
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventNameTextView.setText(event.getEventName());
        holder.eventDescriptionTextView.setText(event.getEventDescription());

        // Set click listener to open OrganizerEventListView and pass the user object
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrganizerEventListView.class);
            intent.putExtra("eventId", event.getEventName()); // Assuming event ID is the event name
            intent.putExtra("User", user); // Pass the user object
            context.startActivity(intent);
        });
    }
     /**
        * Returns the total number of items in the data set.
            *
            * @return The number of events in the list
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for each event item, holding views for event name and description.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventDescriptionTextView;

        /**
         * Constructor that binds views from the layout to fields in the ViewHolder.
         *
         * @param itemView The view associated with this ViewHolder
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
        }
    }
}
