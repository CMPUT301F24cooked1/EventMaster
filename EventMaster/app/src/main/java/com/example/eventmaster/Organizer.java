package com.example.eventmaster;

import java.io.Serializable;
import java.util.ArrayList;

public class Organizer implements Serializable {
    private ArrayList<String> eventIds;

    public Organizer(ArrayList<String> eventIds) {
        this.eventIds = eventIds;
    }

    public Organizer() {
        this.eventIds = new ArrayList<String>();
    }

    public ArrayList<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(ArrayList<String> eventIds) {
        this.eventIds = eventIds;
    }
}
