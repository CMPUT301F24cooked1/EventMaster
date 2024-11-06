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

public class ViewEventsAdapter extends RecyclerView.Adapter<ViewEventsAdapter.EventViewHolder> {
    /**
     * Initializes the event adapter
     * @param eventList list of events that will be displayed
     * @param context context that the adapter is being used
     * @param parent where the new view will be added
     * @param viewType type of the new view
     * @param holder will update the position of all the items being displayed
     * @param position position of the item
     * Creates an adapter to display all Events on the view events screen
     */
    private List<Event> eventList;
    private Context context;
    private Profile user;

    public ViewEventsAdapter(List<Event> eventList, Context context, Profile user) {
        this.eventList = eventList;
        this.context = context;
        this.user = user;
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
        holder.bind(event, context, user);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the event adapter
         * @param itemView the view that will be held
         * @param event event data
         * @param context context to start activities
         * Create an adapter to display all Events on the view events screen
         */
        TextView eventNameTextView;
        TextView eventDescriptionTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
        }

        public void bind(Event event, Context context, Profile user) {

            // set the text on the recycler view
            eventNameTextView.setText(event.getEventName());   // grab facility name/device id
            eventDescriptionTextView.setText(event.getEventDescription());  // grab description from events

            // Handle the click event
            itemView.setOnClickListener(v -> {
                String deviceID = event.getDeviceID();
                // Send information over to Start QR scanner activity
                Intent intent = new Intent(context, QRScanFragment.class);
                intent.putExtra("event", event.getEventName());
                intent.putExtra("deviceID", event.getDeviceID());
                intent.putExtra("User", user);

                //Toast.makeText(context, "facility id: "+ deviceID, Toast.LENGTH_SHORT).show();
                context.startActivity(intent);
            });
        }
    }
}


