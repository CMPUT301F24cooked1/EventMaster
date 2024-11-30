package com.example.eventmaster;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter class for displaying the waitlisted events of an entrant in a RecyclerView
 */

public class ViewJoinedEventsAdapter extends RecyclerView.Adapter<ViewJoinedEventsAdapter.EventViewHolder> {


    private List<Event> eventList;
    private Context context;
    private String deviceID;
    private FirebaseFirestore db;
    private String hashData;
    private Profile user;



    public ViewJoinedEventsAdapter(List<Event> eventList, Context context, Profile user) {
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
        holder.bind(event, context);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the waitlisted event adapter
         * @param itemView the view that will be held
         * @param event event data
         * @param context context to start activities
         * Create an adapter to display all Waitlisted Events on the view waitlist screen
         */
        TextView eventNameTextView;
        TextView eventDescriptionTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
        }

        public void bind(Event event, Context context) {

            // set the text on the recycler view
            eventNameTextView.setText(event.getEventName());   // grab facility name/device id
            eventDescriptionTextView.setText(event.getEventDescription());  // grab description from events

            // Handle the click event
            itemView.setOnClickListener(v -> {

                deviceID = event.getDeviceID();
                fetchHashData(deviceID, event, new FirestoreCallback<String>() {
                    @Override
                    public void onCallback(String data) {
                        // Send information over to unjoin waitlist screen
                        Intent intent = new Intent(context, JoinedEventDetailsActivity.class);
                        intent.putExtra("event", event.getEventName());
                        intent.putExtra("deviceID", event.getDeviceID());
                        intent.putExtra("hashed_data", getHashData());
                        intent.putExtra("User", user);
                        Log.d("Waitlistedeventsadapter", "Event hash data " + getHashData());
                        //Toast.makeText(context, "facility id: "+ deviceID, Toast.LENGTH_SHORT).show();
                        context.startActivity(intent);
                    }
                });
            });
        }
    }

    /**
     * Gets the unique hash data for the specific event
     * @param deviceID
     * @param event
     * @param callback
     */
    public void fetchHashData(String deviceID, Event event, FirestoreCallback<String> callback){
        db = FirebaseFirestore.getInstance();
        //access the hash data through the entrant document
        DocumentReference eventRef = db.collection("entrants")
                .document(deviceID)
                .collection("Waitlisted Events")
                .document(event.getEventName());
        eventRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            hashData = documentSnapshot.getString("hashed_data");
                            callback.onCallback(hashData); //be able to recieve the hash data properly
                            System.out.println("Waitlisted Event: " + hashData);
                        } else {
                            System.out.println("Document does not exist.");
                            callback.onCallback(null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        System.err.println("Error retrieving document: " + e.getMessage());
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * returns the hash data for the specific event
     * @return
     */
    public String getHashData() {
        return hashData;
    }


}
