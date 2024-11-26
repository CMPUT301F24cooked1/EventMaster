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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 *  Adapter class for displaying the list of profiles in a RecyclerView
 *  Each item in the list corresponds to a profile with its profile details
 */
public class ViewQRAdapter extends RecyclerView.Adapter<ViewQRAdapter.QRViewHolder>{
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
    private ArrayList<AbstractMap.SimpleEntry<String, String>> qrList = new ArrayList<>();
    private ArrayList<AbstractMap.SimpleEntry<String, String>> selectedQRList = new ArrayList<>();
    private ArrayList<AbstractMap.SimpleEntry<String, String>> selectedQRListStorage = new ArrayList<>();

    private Context context;
    private Profile user;
    private Boolean isAdmin = false;
    private Boolean showCheckBox = false;
    private Boolean isClickable = true;
    private FirebaseFirestore firestore;

    public ViewQRAdapter(ArrayList<AbstractMap.SimpleEntry<String, String>> qrList, Context context, Profile user, Boolean isAdmin) {
        this.qrList = qrList;
        this.context = context;
        this.user = user;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewQRAdapter.QRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qr_item, parent, false);
        return new ViewQRAdapter.QRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRViewHolder holder, int position) {
        String eventName = qrList.get(position).getKey();
        String hash = qrList.get(position).getValue();
        holder.bind(eventName, hash, context, user);
        holder.itemView.setClickable(isClickable);


        // Checkbox stuff ------------------------------------
        // set the checkbox state
        if (selectedQRList != null) {
            // Set the CheckBox state
            holder.checkBox.setChecked(selectedQRList.contains(qrList.get(position)));
        } else {
            holder.checkBox.setChecked(false);
        }
        // adds an event to the selectedEvents list when it's respective checkbox is checked
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedQRList.contains(qrList.get(position))) {
                    selectedQRList.add(qrList.get(position));
                    selectedQRListStorage.add(qrList.get(position));

                }
            } else {
                selectedQRList.remove(qrList.get(position));
            }
        });

        if (isAdmin){
            holder.arrow.setVisibility((View.GONE));
        } else {
            holder.arrow.setVisibility((View.VISIBLE));
        }

        holder.checkBox.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);

        // display profile picture
        //if (profile.getName() != null){
        //    ProfilePicture.loadProfilePicture(profile, holder.profilePicture, context);
        //}
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
    public void deleteSelectedQRs() {
        firestore = FirebaseFirestore.getInstance();
        if (!selectedQRListStorage.isEmpty()) {
            CollectionReference facilitiesRef = firestore.collection("facilities");
            Log.d("Firestore Debug", "Attempting to fetch facilities");

            facilitiesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firestore Debug", "Facilities fetched successfully");
                    Log.d("Firestore Debug", "Selected events: " + selectedQRListStorage);

                    List<Task<Void>> deleteTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot facilityDoc : task.getResult()) {
                        // Retrieve events for each facility
                        CollectionReference eventsRef = facilityDoc.getReference().collection("My Events");
                        eventsRef.get().addOnCompleteListener(eventTask -> {
                            if (eventTask.isSuccessful()) {
                                Log.d("Firestore success", "Fetched events successfully");
                                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                    String eventName = eventDoc.getString("eventName");
                                    String hash = eventDoc.getString("hash");
                                    Log.d("Current Event", "current hash: " + hash);
                                    AbstractMap.SimpleEntry<String, String> tuple = new AbstractMap.SimpleEntry<>(eventName, hash);
                                    String documentId = eventDoc.getId();

                                    if (selectedQRListStorage.contains(tuple)) {
                                        // Use the document ID to delete the event
                                        DocumentReference eventRef = eventsRef.document(documentId);
                                        Task<Void> deleteTask = eventRef.update("hash", FieldValue.delete()).addOnSuccessListener(aVoid -> {
                                            qrList.remove(tuple);
                                            notifyDataSetChanged();

                                        }).addOnFailureListener(e ->{
                                            Log.d("QRADAPTER", "failed to delete" + e.getMessage());
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
        return qrList.size();
    }

    public class QRViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the event adapter
         * @param itemView the view that will be held
         * @param event event data
         * @param context context tSo start activities
         * Create an adapter to display all Events on the view events screen
         */
        TextView eventNameTextView;
        TextView hashTextView;
        CheckBox checkBox;
        ImageView arrow;


        public QRViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name_qr);
            hashTextView = itemView.findViewById(R.id.qrHashCode);
            checkBox = itemView.findViewById(R.id.checkbox);
            arrow = itemView.findViewById(R.id.arrow);
        }

        public void bind(String eventName, String hash, Context context, Profile user) {

            // set the text on the recycler view
            eventNameTextView.setText(eventName);
            hashTextView.setText(hash);
        }
    }
}