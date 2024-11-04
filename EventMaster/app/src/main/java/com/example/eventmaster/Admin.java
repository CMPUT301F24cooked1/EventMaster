package com.example.eventmaster;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.qrcode.encoder.QRCode;

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

    ArrayList<Event> events = new ArrayList<>();
    ArrayList<String> profiles = new ArrayList<>();
    ArrayList<String> images = new ArrayList<>();     // IMPORTANT: modify images to proper data type
    ArrayList<String> QRCodes = new ArrayList<>();    // IMPORTANT: modify qrcodes to proper data type
    ArrayList<String> facilities = new ArrayList<>(); // IMPORTANT: modify facilities to proper data type


    /**
     * Gets all of the phone ID's in the profiles collection of the Firestore Database
     * @param callback the interface that transfers the list of phone ID's to caller
     */
    void getAllEvents(FirestoreCallback<ArrayList<Event>> callback){
        db.collection(eventCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()){
                                events.add((Event) document.getData()); //adds the events to the events list
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            callback.onCallback(events);
                        } else {
                            Log.d(TAG, "Error getting events: ", task.getException());
                            callback.onCallback(new ArrayList<>());  // null arraylist to handle errors
                        }
                    }
                });
    }

    /**
     * Gets all of the phone ID's in the profiles collection of the Firestore Database
     * @param callback the interface that transfers the list of phone ID's to caller
     */
    void getAllProfiles(FirestoreCallback<ArrayList<String>> callback){
        db.collection(profileCollection)
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
                    Log.d(TAG, "Error getting phone ID's: ", task.getException());
                    callback.onCallback(new ArrayList<>());  // null arraylist to handle errors
                }
            }
        });
    }

    /**
     * Gets all of the phone ID's in the profiles collection of the Firestore Database
     * @param callback the interface that transfers the list of images to caller
     *                 NOTE: CHANGE ArrayList type from String to proper datatype
     */
    void getAllImages(FirestoreCallback<ArrayList<String>> callback){
        db.collection(imageCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()){
                                images.add(document.getData().toString()); //add image to images list (IMAGE DATA TYPE NOT KNOWN SO I PUT toString()
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            callback.onCallback(images);
                        } else {
                            Log.d(TAG, "Error getting images: ", task.getException());
                            callback.onCallback(new ArrayList<>());  // null arraylist to handle errors
                        }
                    }
                });
    }

    /**
     * Gets all of the phone ID's in the profiles collection of the Firestore Database
     * @param callback the interface that transfers the list of images to caller
     *                 NOTE: change arraylist type from String to proper data type
     */
    void getAllQRCodes(FirestoreCallback<ArrayList<String>> callback){
        db.collection(QRCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()){
                                QRCodes.add(document.getData().toString()); //add qrcode to QRCodes list (qrcode DATA TYPE NOT KNOWN SO I PUT toString()
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            callback.onCallback(QRCodes);
                        } else {
                            Log.d(TAG, "Error getting qr codes: ", task.getException());
                            callback.onCallback(new ArrayList<>());  // null arraylist to handle errors
                        }
                    }
                });
    }

    /**
     * Gets all of the phone ID's in the profiles collection of the Firestore Database
     * @param callback the interface that transfers the list of images to caller
     *                 NOTE: change arraylist type from String to proper data type
     */
    void getAllFacilities(FirestoreCallback<ArrayList<String>> callback){
        db.collection(facilitiesCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()){
                                facilities.add(document.getData().toString()); //add facilities to facilities list (facilities DATA TYPE NOT KNOWN SO I PUT toString()
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            callback.onCallback(facilities);
                        } else {
                            Log.d(TAG, "Error getting qr codes: ", task.getException());
                            callback.onCallback(new ArrayList<>());  // null arraylist to handle errors
                        }
                    }
                });
    }

    /**
     * Removes the event from the database collection
     * @param event the event object to be deleted
     */
    void deleteEvent(Event event){
        db.collection(eventCollection).document(event.getDeviceID()).delete().addOnSuccessListener(aVoid ->{
            // Event is deleted from firestore
            Log.d("Firestore", "Document successfully deleted!");
        }).addOnFailureListener(e -> {
            Log.d("Firestore", "Error deleting document", e);
        });
    }

    /**
     * Removes the profile from the database collection
     * @param profile the profile object to be deleted
     */
    void deleteProfile(Profile profile){
        db.collection(eventCollection).document(profile.getDeviceId()).delete().addOnSuccessListener(aVoid ->{
            // Profile is deleted from firestore
            Log.d("Firestore", "Document successfully deleted!");
        }).addOnFailureListener(e -> {
            Log.d("Firestore", "Error deleting document", e);
        });
    }

    /*
        /**
         * Removes the qr code from the database collection
         * @param qrCode the qrCode object to be deleted
         *               NOTE: DOES NOT WORK SINCE THERE IS NO QRcode CLASS RIGHT NOW

    void deleteQRCode(){
        db.collection(eventCollection).document(qrCode.getDeviceID()).delete().addOnSuccessListener(aVoid ->{
            // qrCode is deleted from firestore
            Log.d("Firestore", "Document successfully deleted!");
        }).addOnFailureListener(e -> {
            Log.d("Firestore", "Error deleting document", e);
        });
    }
    */

    /**
     * Removes the facility from the database collection
     * @param facility the facility object to be deleted
     */
    void deleteFacility(Facility facility){
        db.collection(eventCollection).document(facility.getDeviceID()).delete().addOnSuccessListener(aVoid ->{
            // Facility is deleted from firestore
            Log.d("Firestore", "Document successfully deleted!");
        }).addOnFailureListener(e -> {
            Log.d("Firestore", "Error deleting document", e);
        });
    }

    /**
     * Removes the event from the database collection
     * @param image the image object to be deleted
     *              NOTE: not sure how to get the image from the firestore database
     */
    void deleteImage(Image image){
        db.collection(eventCollection).document(image.getImageID()).delete().addOnSuccessListener(aVoid ->{
            // Event is deleted from firestore
            Log.d("Firestore", "Document successfully deleted!");
        }).addOnFailureListener(e -> {
            Log.d("Firestore", "Error deleting document", e);
        });
    }

}
