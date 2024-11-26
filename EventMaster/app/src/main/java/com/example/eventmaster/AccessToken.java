package com.example.eventmaster;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

//https://www.youtube.com/watch?v=o_PikvavsYY, 2024-11-24
public class AccessToken {

    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken(String jsonString) {

        try {

            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream).createScoped(Collections.singleton(firebaseMessagingScope));

            googleCredentials.refresh();

            Log.d("Access Token", "Retrieved Access Token.");

            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            Log.e("error", "Failed to retrieve access token. " + e.getMessage());
        }
        return "";
    }
}
