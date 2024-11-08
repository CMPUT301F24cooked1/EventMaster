package com.example.eventmaster;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;
    private Profile user; // Add a Profile member variable

    // Update the constructor to accept 'user'
    public EventAdapter(List<Event> eventList, Context context, Profile user) {
        this.eventList = eventList;
        this.context = context;
        this.user = user; // Store the user object
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventDescriptionTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
        }
    }
}
