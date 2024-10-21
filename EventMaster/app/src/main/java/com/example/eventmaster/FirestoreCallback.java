package com.example.eventmaster;

import java.util.ArrayList;

/**
 * Due to the request to the database being asynchronous, I implemented a "callback" interface.
 * This will retrieve the data after the method has finished
 */
public interface FirestoreCallback<T>{
    void onCallback(T data);
}
