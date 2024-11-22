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


    public NotificationsAdapter(List<Event> eventList, Context context, Profile user) {
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

    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        if (event != null) {
            Log.d("AdapterDebug", "Binding Event: " + event.getEventName() + ", Type: " + event.getNotificationType());
            holder.bind(event, context); // Call the bind method
        }
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
        TextView eventDescriptionTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);

        }

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
        }
    }
}


