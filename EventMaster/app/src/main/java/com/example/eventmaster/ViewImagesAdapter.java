package com.example.eventmaster;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 *  Adapter class for displaying the list of images in a RecyclerView
 *  Each item in the list corresponds to an image
 */
public class ViewImagesAdapter extends RecyclerView.Adapter<ViewImagesAdapter.ImageViewHolder>{
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
    private List<String> imageList;
    private ArrayList<String> selectedImages = new ArrayList<>();
    private Context context;
    private Profile user;
    private Boolean isAdmin = false;
    private Boolean showCheckBox = false;
    private Boolean isClickable = true;
    private FirebaseFirestore firestore;


    public ViewImagesAdapter(List<String> imageList, Context context, Profile user, Boolean isAdmin) {
        this.imageList = imageList;
        this.context = context;
        this.user = user;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewImagesAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ViewImagesAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewImagesAdapter.ImageViewHolder holder, int position) {
        String imageUrl = imageList.get(position);

        Glide.with(context)
                .load(imageUrl) // Updated URL
                .placeholder(R.drawable.default_poster)
                .error(R.drawable.error)
                .into(holder.imageView);

        holder.itemView.setClickable(isClickable);

        // Checkbox stuff ------------------------------------
        // set the checkbox state
        if (selectedImages != null) {
            // Set the CheckBox state
            holder.checkBox.setChecked(selectedImages.contains(imageList.get(position)));
        } else {
            holder.checkBox.setChecked(false);
        }
        // adds an event to the selectedEvents list when it's respective checkbox is checked
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedImages.contains(imageList.get(position))) {
                    selectedImages.add(imageList.get(position));
                }
            } else {
                selectedImages.remove(imageList.get(position));
            }
        });

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
    public void deleteSelectedImages() {
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        /**
         * Initializes the event adapter
         * @param itemView the view that will be held
         * Create an adapter to display all images on the admin images screen
         */
        ImageView imageView;
        CheckBox checkBox;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.admin_image);
            checkBox = itemView.findViewById(R.id.remove_profile_checkbox);
        }
    }
}
