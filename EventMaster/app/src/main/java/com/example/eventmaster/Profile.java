package com.example.eventmaster;

public class Profile {
    private String deviceId;

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
}
