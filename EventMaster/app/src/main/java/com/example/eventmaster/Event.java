package com.example.eventmaster;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String eventName;
    private String eventDescription;
    private int eventCapacity;
    private int waitlistCapacity;
    private Date eventFinalDate;
    private boolean geolocation;
    private ArrayList<Entrant> waitlist;
    //private QRCode eventQR;

    public Event(String eventName, String eventDescription, int eventCapacity, int waitlistCapacity, Date eventFinalDate, boolean geolocation) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventCapacity = eventCapacity;
        this.waitlistCapacity = waitlistCapacity;
        this.eventFinalDate = eventFinalDate;
        this.geolocation = geolocation;
        //this.eventQR = new QRCode(eventName);
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
}
