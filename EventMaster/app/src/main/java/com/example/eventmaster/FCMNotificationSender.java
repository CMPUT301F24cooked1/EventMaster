package com.example.eventmaster;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to create and send a notification to another user.
 */
public class FCMNotificationSender {
    //https://www.youtube.com/watch?v=S69IdS0FZyQ, 2024-11-23

    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context context;
    private final String postUrl = "https://fcm.googleapis.com/v1/projects/eventmaster-27b75/messages:send";

    /**
     * Constructs a FCMNotificationSender object, with all the details needed to send a notification
     * @param userFcmToken Token of the device to send the notification to.
     * @param title The notification title.
     * @param body The notification body.
     * @param context The context of the notification.
     */
    public FCMNotificationSender(String userFcmToken, String title, String body, Context context) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.context = context;
    }

    /**
     * Sends (or Queues) notification to user based on data in object
     * @param jsonFile the private key from firestore used to get access token.
     */
    public void SendNotifications(String jsonFile) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject messageObject = new JSONObject();

            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", title);
            notificationObject.put("body", body);

            messageObject.put("token", userFcmToken);
            messageObject.put("notification", notificationObject);

            mainObj.put("message", messageObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, response -> {

            }, volleyError -> {

            }) {

                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    AccessToken accessToken = new AccessToken();
                    String accessKey = accessToken.getAccessToken(jsonFile);
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "Bearer " + accessKey);
                    return header;
                }
            };

            requestQueue.add(request);
            Log.d("Notification", "Request added to queue.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}