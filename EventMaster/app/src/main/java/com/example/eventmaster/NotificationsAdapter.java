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

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.EventViewHolder> {
    private List<Event> eventList;
    private Context context;
    private String deviceID;
    private FirebaseFirestore db;
    private String hashData;
    private Profile user;
    private OnNotificationClickListener listener;



    public NotificationsAdapter(List<Event> eventList, Context context, Profile user, OnNotificationClickListener listener) {
        this.eventList = eventList;
        this.context = context;
        this.user = user;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }
//    @Override
//    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
//        Event event = eventList.get(position);
//        holder.bind(event, context);
//    }
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        Log.d("Debug", "Binding Event: " + event.getEventName()); // Log for debugging
        if (event != null && event.getEventName() != null) {
            holder.eventNameTextView.setText(event.getEventName());
        } else {
            holder.eventNameTextView.setText("No Event Name");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(event.getEventName(), event.getDeviceID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }


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
        // TextView eventDescriptionTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);


            // eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
        }

        public void bind(Event event, Context context) {
            eventNameTextView.setText(event.getEventName());  // Set the event name to the TextView
        }
    }

    public interface OnNotificationClickListener {
        void onNotificationClick(String eventName, String facilityID);
    }
}


