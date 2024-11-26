package com.example.eventmaster;

import java.io.Serializable;

/**
 * The Profile class represents the user's profile information. It stores the personal details of the users,
 * this includes the device ID, email, phone number, and admin password. it also includes enabling/disabling notifications.
 *
 * <p>The class implements Serializable to allow profile data to be passed between activities</p>
 *
 */
public class Profile implements Serializable {
    private String deviceId;
    private boolean notificationSwitch;
    private String name;
    private String email;
    private String phone_number;
    private String password;

    public Profile() {
        //Default constructor
    }

    /**
     * Constructs a new Profile object with all the details
     *
     * This constructor initializes the profile with the given device ID, name, email, and phone number.
     * @param deviceId The unique device ID associated with the user's device
     * @param name Name of the user
     * @param email Email of the user
     * @param phone_number Phone number of the user
     */
    public Profile(String deviceId, String name, String email, String phone_number) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone_number = phone_number;
    }


    /**
     * Sets the deviceId of the user.
     *
     * @param deviceId the deviceID of the user's device
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Sets the name of the user.
     *
     * @param name the name of the user
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Gets the deviceId of the user.
     *
     * @return the deviceId of the user
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the name of the user.
     *
     * @return the name of the user
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the user's email
     *
     * @return user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user
     *
     * @param email email of the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get user's phone number
     *
     * @return user's phone number
     */
    public String getPhone_number() {
        return phone_number;
    }

    /**
     * Set user's phone number
     *
     * @param phone_number the user's phone number
     */
    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    /**
     * Gets the admin password
     *
     * @return admin password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the admin password
     *
     * @param password admin's password
     */
    public void setPassword(String password) {
        this.password = password;
    }



    /**
     * Sets the notifications to being enabled or disabled
     *
     * @param toggleNotifs changing the notifications to on or off
     */
    public void setNotifications(boolean toggleNotifs){
        this.notificationSwitch = toggleNotifs;
    }

    /**
     * Get the notifications to being enabled or disabled
     *
     * @return true or false if user wants notifs on or off
     */
    public boolean getNotifications(){
        return notificationSwitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return deviceId != null && deviceId.equals(profile.deviceId);
    }


}
