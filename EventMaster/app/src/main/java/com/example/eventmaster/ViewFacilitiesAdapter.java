package com.example.eventmaster;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  Adapter class for displaying the list of images in a RecyclerView
 *  Each item in the list corresponds to an image
 */
public class ViewFacilitiesAdapter extends RecyclerView.Adapter<ViewFacilitiesAdapter.FacilityViewHolder>{
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
    private List<Facility> facilityList;
    private ArrayList<Facility> selectedFacilities = new ArrayList<>();
    private ArrayList<Facility> selectedFacilitiesStorage = new ArrayList<>();

    private Context context;
    private Profile user;
    private Boolean isAdmin = false;
    private Boolean showCheckBox = false;
    private Boolean isClickable = true;
    private FirebaseFirestore firestore;


    public ViewFacilitiesAdapter(List<Facility> facilityList, Context context, Profile user, Boolean isAdmin) {
        this.facilityList = facilityList;
        this.context = context;
        this.user = user;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewFacilitiesAdapter.FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.facility_item, parent, false);
        return new ViewFacilitiesAdapter.FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewFacilitiesAdapter.FacilityViewHolder holder, int position) {
        Facility facility = facilityList.get(position);
        holder.bind(facility, context, user);

        if (isAdmin){
            holder.itemView.setClickable(false);
        } else{
            holder.itemView.setClickable(isClickable);
        }

        // Checkbox stuff ------------------------------------
        // set the checkbox state
        if (selectedFacilities != null) {
            // Set the CheckBox state
            holder.checkBox.setChecked(selectedFacilities.contains(facilityList.get(position)));
        } else {
            holder.checkBox.setChecked(false);
        }
        // adds an event to the selectedEvents list when it's respective checkbox is checked
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedFacilities.contains(facilityList.get(position))) {
                    selectedFacilities.add(facilityList.get(position));
                    selectedFacilitiesStorage.add(facilityList.get(position));
                }
            } else {
                selectedFacilities.remove(facilityList.get(position));
            }
        });
        if (isAdmin){
            holder.arrow.setVisibility((View.GONE));
        } else {
            holder.arrow.setVisibility((View.VISIBLE));
        }

        holder.checkBox.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);
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
    public void deleteSelectedFacilities() {
        firestore = FirebaseFirestore.getInstance();
        if (!selectedFacilitiesStorage.isEmpty()) {
            CollectionReference facilitiesRef = firestore.collection("facilities");
            Log.d("Firestore Debug", "Attempting to fetch facilities");

            facilitiesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firestore Debug", "Facilities fetched successfully");
                    Log.d("Firestore Debug", "Selected facilities: " + selectedFacilitiesStorage);
                    List<Task<Void>> deleteTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot facilityDoc : task.getResult()) {
                        Facility facility = facilityDoc.toObject(Facility.class);
                        Log.d("Firestore Debug", "extracted facility: " + facility.getDeviceId());
                        String documentId = facilityDoc.getId();
                        // Retrieve events for each facility
                        if (selectedFacilitiesStorage.contains(facility)) {
                            // Use the document ID to delete the event
                            Task<Void> deleteTask = facilitiesRef.document(documentId).delete().addOnSuccessListener(aVoid -> {
                                Log.d("Facility Deletion", "Facility deleted successfully with ID: " + documentId);
                                facilityList.remove(facility); // Ensure dataset consistency
                                notifyDataSetChanged();
                            }).addOnFailureListener(e -> {
                                Log.e("Event Deletion", "Error deleting facility: " + e.getMessage());
                            });

                            // Add this task to the list
                            deleteTasks.add(deleteTask);
                        }
                    }
                    // Wait for all delete tasks to complete
                    Tasks.whenAll(deleteTasks).addOnCompleteListener(allTasks -> {
                        if (allTasks.isSuccessful()) {
                            Log.d("Firestore Debug", "All selected facilities deleted successfully");
                        } else {
                            Log.e("Firestore Debug", "Error deleting some events", allTasks.getException());
                        }
                        // Notify the adapter once all deletions are done
                        notifyDataSetChanged();
                    });
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    public class FacilityViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the event adapter
         * @param itemView the view that will be held
         * Create an adapter to display all images on the admin images screen
         */
        TextView facilityName;
        TextView facilityDescription;
        CheckBox checkBox;
        ImageView arrow;


        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityName = itemView.findViewById(R.id.facilityNameTextView);
            facilityDescription = itemView.findViewById(R.id.facilityDescriptionTextView);
            checkBox = itemView.findViewById(R.id.removeFacilityCheckBox);
            arrow = itemView.findViewById(R.id.facility_item_arrow);
        }

        public void bind(Facility facility, Context context, Profile user){
            String unnamed = "Unnamed Facility";
            Log.d("HDUSH", "facilityname " + facility.getFacilityName());
            if (facility.getFacilityName() == null || facility.getFacilityName().isEmpty()){
                facilityName.setText(unnamed);
            } else {
                facilityName.setText(facility.getFacilityName());
                facilityDescription.setText(facility.getFacilityDesc());
            }

        }
    }
}
