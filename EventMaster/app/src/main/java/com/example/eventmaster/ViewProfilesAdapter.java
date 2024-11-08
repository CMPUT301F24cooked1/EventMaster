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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewProfilesAdapter extends RecyclerView.Adapter<ViewProfilesAdapter.ProfileViewHolder>{
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
    private List<Profile> profileList;
    private ArrayList<Profile> selectedProfiles = new ArrayList<>();
    private Context context;
    private Profile user;
    private Boolean isAdmin = false;
    private Boolean showCheckBox = false;
    private Boolean isClickable = true;
    private FirebaseFirestore firestore;



    public ViewProfilesAdapter(List<Profile> profileList, Context context, Profile user, Boolean isAdmin) {
        this.profileList = profileList;
        this.context = context;
        this.user = user;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewProfilesAdapter.ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ViewProfilesAdapter.ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewProfilesAdapter.ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        holder.bind(profile, context, user);
        holder.itemView.setClickable(isClickable);


        // Checkbox stuff ------------------------------------
        // set the checkbox state
        if (selectedProfiles != null) {
            // Set the CheckBox state
            holder.checkBox.setChecked(selectedProfiles.contains(profileList.get(position)));
        } else {
            holder.checkBox.setChecked(false);
        }
        // adds an event to the selectedEvents list when it's respective checkbox is checked
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedProfiles.contains(profileList.get(position))) {
                    selectedProfiles.add(profileList.get(position));
                }
            } else {
                selectedProfiles.remove(profileList.get(position));
            }
        });

        if (isAdmin){
            holder.arrow.setVisibility((View.GONE));
        } else {
            holder.arrow.setVisibility((View.VISIBLE));
        }

        holder.checkBox.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);

        // display profile picture
        if (profile.getName() != null){
            ProfilePicture.loadProfilePicture(profile, holder.profilePicture, context);
        }
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
    public void deleteSelectedProfiles() {
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the event adapter
         * @param itemView the view that will be held
         * @param event event data
         * @param context context tSo start activities
         * Create an adapter to display all Events on the view events screen
         */
        TextView profileName;
        TextView profileDeviceId;
        ImageView profilePicture;
        CheckBox checkBox;
        ImageView arrow;


        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profile_name);
            profileDeviceId = itemView.findViewById(R.id.profile_device_id);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            checkBox = itemView.findViewById(R.id.remove_profile_checkbox);
            arrow = itemView.findViewById(R.id.profile_details_arrow);
        }

        public void bind(Profile profile, Context context, Profile user) {

            // set the text on the recycler view
            profileName.setText(profile.getName());
            profileDeviceId.setText(profile.getDeviceId());

            // Handle the click event
            itemView.setOnClickListener(v -> {
                String deviceID = profile.getDeviceId();
                // Send information over to profile activity
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("User", profile);
                context.startActivity(intent);
            });
        }
    }
}

