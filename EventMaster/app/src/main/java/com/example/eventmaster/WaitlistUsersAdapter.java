package com.example.eventmaster;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying waitlisted users in a RecyclerView.
 * This adapter fetches user details (name and profile picture) from Firestore and binds them to the view.
 */
public class WaitlistUsersAdapter extends RecyclerView.Adapter<WaitlistUsersAdapter.UserViewHolder> {

    /**
     * User class to hold the details of a user including name and profile picture URL.
     */
    public static class User {
        String name;
        String profilePictureUrl;

        /**
         * Constructs a new User with the specified name and profile picture URL.
         *
         * @param name The name of the user.
         * @param profilePictureUrl The URL of the user's profile picture.
         */
        public User(String name, String profilePictureUrl) {
            this.name = name;
            this.profilePictureUrl = profilePictureUrl;
        }
    }

    private List<User> userList; // Updated to hold User objects
    private Context context;
    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;

    /**
     * Constructs a new WaitlistUsersAdapter with the specified parameters.
     *
     * @param userList A list of users to display.
     * @param context The context in which the adapter operates.
     * @param eventId The ID of the event for which users are waitlisted.
     */
    public WaitlistUsersAdapter(List<User> userList, Context context, String eventId) {
        this.userList = userList;
        this.context = context;
        this.eventId = eventId;
        db = FirebaseFirestore.getInstance();

        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.waitlist_user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position); // Now this is a User object with name and profile picture
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for binding a user to a RecyclerView item.
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView;
        ImageView profileImageView;


        /**
         * Constructs a new UserViewHolder.
         *
         * @param itemView The view of the item being displayed.
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userTextView = itemView.findViewById(R.id.userTextView);
            profileImageView = itemView.findViewById(R.id.profile_picture);
        }

        /**
         * Binds the user data to the view components.
         *
         * @param user The user object containing the data to bind.
         */
        public void bind(User user) {
            userTextView.setText(user.name);

            // Check if profilePictureUrl is not null or empty before loading with Glide
            if (user.profilePictureUrl != null && !user.profilePictureUrl.isEmpty()) {
                Glide.with(context)
                        .load(user.profilePictureUrl)
                        .placeholder(R.drawable.profile_picture) // Placeholder image while loading
                        .circleCrop() // Apply circle crop transformation
                        .into(profileImageView);
            } else {
                // Set a default profile picture if URL is null or empty
                profileImageView.setImageResource(R.drawable.profile_picture); // Use a default drawable
            }
        }
    }

    /**
     * Fetches the list of waitlisted users from Firestore and adds them to the user list.
     */
    public void fetchWaitlistedUsers() {
        DocumentReference eventRef = db.collection("facilities")
                .document(deviceId)
                .collection("My Events")
                .document(eventId);

        eventRef.collection("waitlist list").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            List<User> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : snapshot) {
                                String userDeviceId = document.getId();
                                fetchUserDetails(userDeviceId, users);
                            }
                        }
                    } else {
                        Log.e("WaitlistUsersAdapter", "Error getting waitlist users", task.getException());
                    }
                });
    }

    /**
     * Fetches the name and profile picture URL of a user based on their device ID.
     *
     * @param userDeviceId The device ID of the user.
     * @param users The list to store fetched users.
     */
    private void fetchUserDetails(String userDeviceId, List<User> users) {
        db.collection("profiles").document(userDeviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");

                        if (name != null && profilePictureUrl != null) {
                            users.add(new User(name, profilePictureUrl));
                            userList.clear();
                            userList.addAll(users);
                            notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("WaitlistUsersAdapter", "Error fetching user details", e));
    }
}
