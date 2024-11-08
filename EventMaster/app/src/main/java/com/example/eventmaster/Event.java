package com.example.eventmaster;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * The Event class represents a single event on the app. It contains all of the information needed for a single event.
 *
 * <p>The class implements Serializable to allow event data to be passed between activities</p>
 *
 */
@IgnoreExtraProperties
public class Event implements Serializable {
    private String eventName;
    private String eventDescription;
    private String deviceID;
    private int eventCapacity;
    private int waitlistCapacity;
    private Date eventFinalDate;
    private boolean geolocation;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Event.class)
     */
    public Event() {

    }

    /**
     * Constructs a new Event object with all the details
     * This constructor initializes the event with the given name, description, device ID, capacity for the event,
     * capacity for the waitlist, the date the event closes, and whether it has geolocation enabled.
     * @param eventName The name of the Event.
     * @param eventDescription The description of the Event.
     * @param deviceID Device ID of the user who made the Event.
     * @param eventCapacity The final capacity for the Event (the number that will be sampled)
     * @param waitlistCapacity The capacity of entrants allowed to join the waitlist.
     * @param eventFinalDate Final date the event closes.
     * @param geolocation The final capacity for the Event (the number that will be sampled)
     */
    public Event(String eventName, String eventDescription, String deviceID, int eventCapacity, int waitlistCapacity, Date eventFinalDate, boolean geolocation) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.deviceID = deviceID;
        this.eventCapacity = eventCapacity;
        this.waitlistCapacity = waitlistCapacity;
        this.eventFinalDate = eventFinalDate;
        this.geolocation = geolocation;
    }

    /**
     * Gets the name of the Event
     *
     * @return the event's name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the eventName
     *
     * @param eventName the name of the Event
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Gets the description of the Event
     *
     * @return the event's description
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * Sets the eventDescription
     *
     * @param eventDescription the description of the Event
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
     * Gets the ID of the event (deviceID acts as the eventID)
     * @return the event's string identifier
     */
    public String getDeviceID(){
        return deviceID;
    }

    /**
     * Sets the ID of the event
     * @param deviceID the event identifier
     */
    public void setDeviceID(String deviceID){
        this.deviceID = deviceID;
    }

    /**
     * Gets the entrant capacity of the Event
     *
     * @return the event capacity
     */
    public int getEventCapacity() {
        return eventCapacity;
    }

    /**
     * Sets the eventCapacity
     *
     * @param eventCapacity the final capacity of the Event
     */
    public void setEventCapacity(int eventCapacity) {
        this.eventCapacity = eventCapacity;
    }

    /**
     * Gets the capacity of the Event's waitlist
     *
     * @return the event's waitlist capacity
     */
    public int getWaitlistCapacity() {
        return waitlistCapacity;
    }

    /**
     * Sets the waitlistCapacity
     *
     * @param waitlistCapacity the capacity of the Event waitlist
     */
    public void setWaitlistCapacity(int waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }

    /**
     * Gets the date when entry onto the waitlist closes
     *
     * @return the event's final date
     */
    public Date getEventFinalDate() {
        return eventFinalDate;
    }

    /**
     * Sets the eventFinalDate
     *
     * @param eventFinalDate the date when ability to join the waitlist closes, and entrants are chosen
     */
    public void setEventFinalDate(Date eventFinalDate) {
        this.eventFinalDate = eventFinalDate;
    }

    /**
     * Checks if geolocation is turned on
     *
     * @return the event's geolocation status
     */
    public boolean isGeolocation() {
        return geolocation;
    }

    /**
     * Sets the event geolocation
     *
     * @param geolocation a boolean that denotes whether the Event Organizer can see the geolocation of Entrants
     */
    public void setGeolocation(boolean geolocation) {
        this.geolocation = geolocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return deviceID != null && deviceID.equals(event.deviceID);
    }

    @Override
    public int hashCode() {
        return deviceID!= null ? deviceID.hashCode() : 0;
    }
    @Override
    public String toString() {
        return "Event{id='" + deviceID + "', name='" + eventName + "'}";
    }
}


