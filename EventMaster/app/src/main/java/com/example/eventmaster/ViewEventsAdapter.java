package com.example.eventmaster;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Adapter class for displaying the list of events in a RecyclerView
 *  Each item in the list corresponds to a specific event with its details
 */
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
    private ArrayList<Event> selectedEvents = new ArrayList<>();
    private ArrayList<Event> selectedEventsStorage = new ArrayList<>();
    private Context context;
    private Profile user;
    private Boolean isAdmin = false;
    private Boolean showCheckBox = false;
    private Boolean isClickable = true;
    private FirebaseFirestore firestore;

    public ViewEventsAdapter(List<Event> eventList, Context context, Profile user, Boolean isAdmin) {
        this.eventList = eventList;
        this.context = context;
        this.user = user;
        this.isAdmin = isAdmin;
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
        if (isAdmin){
            holder.itemView.setClickable(false);
        } else{
            holder.itemView.setClickable(isClickable);
        }


        // Checkbox stuff ------------------------------------
        // set the checkbox state
        if (selectedEvents != null) {
            // Set the CheckBox state
            holder.eventCheckBox.setChecked(selectedEvents.contains(eventList.get(position)));
        } else {
            holder.eventCheckBox.setChecked(false);
        }
        // adds an event to the selectedEvents list when it's respective checkbox is checked
        holder.eventCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedEvents.contains(eventList.get(position))) {
                    selectedEvents.add(eventList.get(position));
                    selectedEventsStorage.add(eventList.get(position));
                }
            } else {
                selectedEvents.remove(eventList.get(position));
            }
        });

        // sets a checkbox in event_item.xml to be visible, and changes the visibility of the arrow image in event_item.xml to be "gone"
        if (isAdmin){
            holder.eventArrow.setVisibility((View.GONE));
        } else {
            holder.eventArrow.setVisibility((View.VISIBLE));
        }

        holder.eventCheckBox.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);

    }

    /**
     * Toggles the visibility of the checkbox from the event_item.xml file
     */
    public void toggleCheckBoxVisibility() {
        showCheckBox = !showCheckBox;
        isClickable = !showCheckBox;
    }

    /**
     * Deletes all events that were marked by the checkbox
     */
    public void deleteSelectedEvents() {
        firestore = FirebaseFirestore.getInstance();
        if (!selectedEventsStorage.isEmpty()) {
            CollectionReference facilitiesRef = firestore.collection("facilities");
            Log.d("Firestore Debug", "Attempting to fetch facilities");

            facilitiesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firestore Debug", "Facilities fetched successfully");
                    Log.d("Firestore Debug", "Selected events: " + selectedEventsStorage);

                    List<Task<Void>> deleteTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot facilityDoc : task.getResult()) {
                        // Retrieve events for each facility
                        CollectionReference eventsRef = facilityDoc.getReference().collection("My Events");
                        eventsRef.get().addOnCompleteListener(eventTask -> {
                            if (eventTask.isSuccessful()) {
                                Log.d("Firestore success", "Fetched events successfully");
                                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                    Event event = eventDoc.toObject(Event.class);
                                    String documentId = eventDoc.getId();
                                    Log.d("Selected Events", "Selected Events: " + selectedEventsStorage);
                                    Log.d("Current Event", "Current Event: " + event);

                                    if (selectedEventsStorage.contains(event)) {
                                        // Use the document ID to delete the event
                                        DocumentReference eventRef = eventsRef.document(documentId);
                                        Task<Void> deleteTask = eventRef.delete().addOnSuccessListener(aVoid -> {
                                            Log.d("Event Deletion", "Event deleted successfully with ID: " + documentId);
                                            eventList.remove(event); // Ensure dataset consistency
                                            notifyDataSetChanged();
                                        }).addOnFailureListener(e -> {
                                            Log.e("Event Deletion", "Error deleting event: " + e.getMessage());
                                        });

                                        // Add this task to the list
                                        deleteTasks.add(deleteTask);
                                    }
                                }
                            } else {
                                Log.e("Firestore Error", "Error fetching events for facility: ", eventTask.getException());
                            }
                        }).addOnFailureListener(e -> {
                            Log.e("Firestore Failure", "Failed to fetch events", e);
                        });
                    }

                    // Wait for all delete tasks to complete
                    Tasks.whenAll(deleteTasks).addOnCompleteListener(allTasks -> {
                        if (allTasks.isSuccessful()) {
                            Log.d("Firestore Debug", "All selected events deleted successfully");
                        } else {
                            Log.e("Firestore Debug", "Error deleting some events", allTasks.getException());
                        }
                        // Notify the adapter once all deletions are done
                        notifyDataSetChanged();
                    });

                } else {
                    Log.e("Firestore Error", "Error fetching facilities: ", task.getException());
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore Failure", "Failed to fetch facilities", e);
            });
        } else {
            Log.d("Firestore Debug", "No selected events to delete");
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the event adapter
         * @param itemView the view that will be held
         * @param event event data
         * @param context context to start activities
         * Create an adapter to display all Events on the view events screen
         */
        TextView eventNameTextView;
        TextView eventDescriptionTextView;
        CheckBox eventCheckBox;
        ImageView eventArrow;


        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
            eventCheckBox = itemView.findViewById(R.id.removeEventCheckBox);
            eventArrow = itemView.findViewById(R.id.event_details_arrow);
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
                context.startActivity(intent);
            });
        }
    }
}


