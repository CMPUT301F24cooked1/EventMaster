package com.example.eventmaster;

import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.Serializable;

public class Profile implements Serializable {
    private String deviceId;
    private boolean notificationSwitch;

    public Profile(String deviceId) {
        this.deviceId = deviceId;
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
     * Gets the deviceId of the user.
     *
     * @return the deviceId of the user
     */
    public String getDeviceId() {
        return deviceId;
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
