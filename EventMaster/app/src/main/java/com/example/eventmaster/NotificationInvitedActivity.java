package com.example.eventmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * NotificationInvitedActivity handles user interactions with an event invitation.
 * <p>
 * Users can accept or decline an invitation. Based on their choice, the activity updates Firestore records and displays a message that tells them if they accepted/declined.
 * Users can only press 1 button and their choice will be reflected by a message when they click back on the notification.
 * </p>
 */
public class NotificationInvitedActivity extends AppCompatActivity {

    private Profile user;
    private String event_name;
    private String event_detail;
    private String facility_id;
    private FirebaseFirestore firestore;

    /**
     * Initializes the activity, sets up views, and fetches event data.
     *
     * @param savedInstanceState Saved instance state for restoring activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeActivity.applyTheme(NotificationInvitedActivity.this);
        setContentView(R.layout.notification_invited_screen);

        firestore = FirebaseFirestore.getInstance();
        user = (Profile) getIntent().getSerializableExtra("User");
        event_name = getIntent().getStringExtra("event_name");
        facility_id = getIntent().getStringExtra("facility_id");

        if (event_name == null || facility_id == null) {
            //Toast.makeText(this, "Error: Event Name or Facility ID is missing", Toast.LENGTH_LONG).show();
            Log.e("NotificationInvited", "Missing event_name or facility_id");
            finish(); // Exit the activity since required data is missing
            return;
        }

        TextView event_name_text = findViewById(R.id.event_name);
        TextView event_description_text = findViewById(R.id.event_description);
        TextView choice_made = findViewById(R.id.textview);
        AppCompatButton accept_invite = findViewById(R.id.accept_invite);
        AppCompatButton decline_invite = findViewById(R.id.decline_invite);

        event_name_text.setText(event_name);
        event_description_text.setText(event_detail);

        fetchEventDescription(facility_id, event_name, event_description_text);

        fetchChoiceStatus(user.getDeviceId(), event_name, choice_made, accept_invite, decline_invite);

        accept_invite.setOnClickListener(v -> {
            updateFirestoreOnAccept();
            updateChoiceStatusInFirestore("accepted");
            updateUIForChoiceMade(choice_made, "accepted", accept_invite, decline_invite);
        });
        decline_invite.setOnClickListener(v -> {
            updateFirestoreOnDecline();
            updateChoiceStatusInFirestore("declined");
            updateUIForChoiceMade(choice_made, "declined", accept_invite, decline_invite);
        });

        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("User", user);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Fetches the user's choice status (accepted or declined) and updates the UI accordingly.
     *
     * @param userId User ID or device ID
     * @param eventName Event name
     * @param choiceMadeText TextView to display the choice made
     * @param acceptButton Button for accepting the invitation
     * @param declineButton Button for declining the invitation
     */
    private void fetchChoiceStatus(String userId, String eventName, TextView choiceMadeText, AppCompatButton acceptButton, AppCompatButton declineButton) {
        firestore.collection("entrants")
                .document(userId)
                .collection("Invited Events")
                .document(eventName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String choiceStatus = documentSnapshot.getString("choiceStatus");
                        if (choiceStatus != null) {
                            updateUIForChoiceMade(choiceMadeText, choiceStatus, acceptButton, declineButton);
                        }
                    } else {
                        Log.w("Firestore", "No document found for event: " + eventName);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching choice status", e));
    }


    /**
     * Updates the choice status in Firestore.
     *
     * @param status The choice status ("accepted" or "declined")
     */
    private void updateChoiceStatusInFirestore(String status) {
        firestore.collection("entrants")
                .document(user.getDeviceId())
                .collection("Invited Events")
                .document(event_name)
                .update("choiceStatus", status)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Choice status updated successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating choice status", e));
    }

    /**
     * Updates the UI based on the user's choice.
     *
     * @param choiceMadeText TextView to display the choice made
     * @param choiceStatus   The choice status ("accepted" or "declined")
     * @param acceptButton   Button for accepting the invitation
     * @param declineButton  Button for declining the invitation
     */
    private void updateUIForChoiceMade(TextView choiceMadeText, String choiceStatus, AppCompatButton acceptButton, AppCompatButton declineButton) {
        choiceMadeText.setVisibility(View.VISIBLE);
        acceptButton.setVisibility(View.GONE);
        declineButton.setVisibility(View.GONE);

        if ("accepted".equals(choiceStatus)) {
            choiceMadeText.setText("You have accepted the event");
        } else if ("declined".equals(choiceStatus)) {
            choiceMadeText.setText("You have declined the event");
        }
    }

    /**
     * Fetches the event description from Firestore.
     *
     * @param facilityId Facility ID
     * @param eventName Event name
     * @param eventDescriptionText TextView to display the event description
     */
    private void fetchEventDescription(String facilityId, String eventName, TextView eventDescriptionText) {
        if (facilityId == null || eventName == null) {
            //Toast.makeText(this, "Error: Facility ID or Event Name is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("facilities")
                .document(facilityId)
                .collection("My Events")
                .document(eventName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventDescription = documentSnapshot.getString("eventDescription");
                        if (eventDescription != null) {
                            event_detail = eventDescription; // Set the global event_detail
                            eventDescriptionText.setText(event_detail);
                        } else {
                            //Toast.makeText(this, "Event description not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Failed to fetch event description", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching event description", e);
                });
    }

    /**
     * Updates Firestore when the user accepts the invitation.
     */
    private void updateFirestoreOnAccept() {
        removeUserFromInvitedList(facility_id, event_name, user.getDeviceId());
        firestore.collection("facilities")
                .document(facility_id)
                .collection("My Events")
                .document(event_name)
                .collection("attendees list")
                .document(user.getDeviceId())
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Successfully added to attendees list", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Device ID added to attendees list successfully");
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Failed to add to attendees list", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding device ID to attendees list", e);
                });
        firestore.collection("entrants")
                .document(user.getDeviceId())
                .collection("Joined Events")
                .document(event_name)
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Successfully added to attendees list", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Device ID added to attendees list successfully");
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Failed to add to attendees list", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding device ID to attendees list", e);
                });
    }

    /**
     * Updates Firestore when the user declines the invitation.
     */
    private void updateFirestoreOnDecline() {
        // add to decline list, remove off invited list, selectedCount in facilities firebase decreases by 1

        decreaseSelectedCount(facility_id, event_name);
        removeUserFromInvitedList(facility_id, event_name, user.getDeviceId());
        firestore.collection("facilities")
                .document(facility_id)
                .collection("My Events")
                .document(event_name)
                .collection("declined list")
                .document(user.getDeviceId())
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Successfully added to declined list", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Device ID added to declined list successfully");
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Failed to add to declined list", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding device ID to declined list", e);
                });
    }

    /**
     * Removes the user from the invited list in Firestore.
     *
     * @param facilityId Facility ID
     * @param eventName Event name
     * @param deviceId Device ID of the user
     */
    private void removeUserFromInvitedList(String facilityId, String eventName, String deviceId) {
        if (facilityId == null || eventName == null || deviceId == null) {
            Log.e("Firestore", "Cannot remove user: Missing facilityId, eventName, or deviceId");
            return;
        }

        firestore.collection("facilities")
                .document(facilityId)
                .collection("My Events")
                .document(eventName)
                .collection("invited list")
                .document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Successfully removed from invited list", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Device ID removed from invited list successfully");
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Failed to remove from invited list", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error removing device ID from invited list", e);
                });
    }

    /**
     * Decreases the selectedCount field in Firestore by 1.
     *
     * @param facilityId Facility ID
     * @param eventName Event name
     */
    private void decreaseSelectedCount(String facilityId, String eventName) {
        if (facilityId == null || eventName == null) {
            //Toast.makeText(this, "Error: Facility ID or Event Name is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("facilities")
                .document(facilityId)
                .collection("My Events")
                .document(eventName)
                .update("selectedCount", FieldValue.increment(-1)) // Decrease the count by 1
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(this, "Successfully decreased selectedCount", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Decreased selectedCount successfully");
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(this, "Failed to decrease selectedCount", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error decreasing selectedCount", e);
                });
    }



}
