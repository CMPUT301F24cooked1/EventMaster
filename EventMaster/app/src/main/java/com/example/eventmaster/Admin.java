package com.example.eventmaster;

import static android.content.ContentValues.TAG;

import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Responsible for the privileges the admin has
 */
public class Admin {
    private FirebaseFirestore db;
    public Admin(){
        db = FirebaseFirestore.getInstance();
    }

    // Variable names for the collections in the database (in case we change the collection names so it will be easier to change here)
    private String eventCollection = "events";
    private String profileCollection = "profiles";
    private String imageCollection = "images";
    private String QRCollection = "qrcode";
    private String facilitiesCollection = "facilities";

    ArrayList<String> profiles = new ArrayList<>();

    /**
     * Gets all of the phone ID's in the profiles collection of the Firestore Database
     * @param callback the interface that holds the list of phone ID's
     */
    void getAllProfiles(FirestoreCallback<ArrayList<String>> callback){
        db.collection("profiles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()){
                        profiles.add(document.getData().toString()); //adds the phone ID's to the profiles list
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    callback.onCallback(profiles);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    callback.onCallback(new ArrayList<>());  // null arraylist to handle errors
                }
            }
        });
    }


}
