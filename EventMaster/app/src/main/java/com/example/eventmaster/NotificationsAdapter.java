package com.example.eventmaster;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter for displaying notifications in a RecyclerView.
 * <p>
 * This adapter binds event data to the notification list and manages click interactions for each event.
 * It supports displaying notifications for both "Invited" and "Rejected" events.
 * </p>
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;
    private String deviceID;
    private FirebaseFirestore db;
    private String hashData;
    private Profile user;
    private OnNotificationClickListener listener;

    /**
     * Constructor for the NotificationsAdapter.
     *
     * @param eventList List of events to display
     * @param context Context for creating views and starting activities
     * @param user User profile associated with the notifications
     * @param listener Listener for handling notification clicks
     */
    public NotificationsAdapter(List<Event> eventList, Context context, Profile user, OnNotificationClickListener listener) {
        this.eventList = eventList;
        this.context = context;
        this.user = user;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder for an event item.
     *
     * @param parent The parent ViewGroup
     * @param viewType The view type for the new ViewHolder
     * @return A new EventViewHolder instance
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);

        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder at a specified position.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the event in the list
     */
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        Log.d("AdapterDebug", "Binding Event: " + event.getEventName() + ", Type: " + event.getNotificationType());

        if (event != null) {
            holder.bind(event, context); 
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(event.getEventName(), event.getDeviceID());
            }
        });
    }

    /**
     * Returns the total number of events in the list.
     *
     * @return The size of the event list
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for holding event data.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the event adapter
         *
         * @param itemView the view that will be held
         * @param event event data
         * @param context context to start activities
         * Create an adapter to display all Events on the view events screen
         */
        TextView eventNameTextView;
        TextView eventDescriptionTextView;

        /**
         * Initializes the ViewHolder for an event item.
         *
         * @param itemView The view for the event item
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);

        }

        /**
         * Binds an event's data to the ViewHolder.
         *
         * @param event   The event to bind
         * @param context The context for any additional actions
         */
        public void bind(Event event, Context context) {
            eventNameTextView.setText(event.getEventName());  // Set the event name to the TextView

            // display invited notification on notification list
            if (event.getNotificationType().equals("Invited")) {
                eventDescriptionTextView.setText("Congratulations! You have been selected...");
            }

            // display rejected notification on notification list
             if (event.getNotificationType().equals("Rejected")){
                 eventDescriptionTextView.setText("Oh no! Sorry you have not been selected... ");
            }

            if (event.getNotificationType().equals("Waitlists")) {
                eventDescriptionTextView.setText("You are in the waitlist...");
            }

            if (event.getNotificationType().equals("Attendees")) {
                eventDescriptionTextView.setText("You are attending...");
            }

        }
    }
    /**
     * Interface for handling notification clicks.
     */
    public interface OnNotificationClickListener {
        /**
         * Callback when a notification is clicked.
         *
         * @param eventName The name of the event
         * @param facilityID The ID of the facility associated with the event
         */
        void onNotificationClick(String eventName, String facilityID);
    }
}


