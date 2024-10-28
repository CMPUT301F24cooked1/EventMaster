package com.example.eventmaster;

import java.io.Serializable;
import java.util.Optional;

public class Profile implements Serializable {
    private String deviceId;
    private boolean notificationSwitch;
    private String name;
    private String email;
    private transient Optional<String> phone_number;
    private String password;

    public Profile(String deviceId, String name, String email, Optional<String> phone_number) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone_number = phone_number != null ? phone_number : Optional.empty();
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
    public Optional<String> getPhone_number() {
        return phone_number != null ? phone_number : Optional.empty();
    }

    /**
     * Set user's phone number
     *
     * @param phone_number the user's phone number
     */
    public void setPhone_number(String phone_number) {
        this.phone_number = Optional.ofNullable(phone_number);
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



}
