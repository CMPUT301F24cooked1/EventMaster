package com.example.eventmaster;

import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.Serializable;

public class Profile implements Serializable {
    private String deviceId;
    private boolean notificationSwitch;
    private String name;

    public Profile(String deviceId, String name) {
        this.deviceId = deviceId;
        this.name = name;
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
     * Sets the notifications to being enabled or disabled
     *
     * @param toggleNotifs changing the notifications to on or off
     */
    public void setNotifications(boolean toggleNotifs){
        this.notificationSwitch = toggleNotifs;
    }



}
