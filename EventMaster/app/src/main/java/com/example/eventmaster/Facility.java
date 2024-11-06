package com.example.eventmaster;

import java.io.Serializable;

public class Facility implements Serializable {
    private String deviceID;
    private String facilityName;
    private String facilityAddress;
    private String facilityDesc;

    public Facility(String deviceID, String facilityName, String facilityAddress, String facilityDesc) {
        this.facilityName = facilityName;
        this.facilityAddress = facilityAddress;
        this.facilityDesc = facilityDesc;
        this.deviceID = deviceID;
    }

    /**
     * Gets the user's facility name.
     *
     * @return Facility name of the user's Facility.
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * Sets the name of the user's Facility.
     *
     * @param facilityName the name of the user's Facility.
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * Gets the user's facility address.
     *
     * @return Facility address of the user's Facility.
     */
    public String getFacilityAddress() {
        return facilityAddress;
    }

    /**
     * Sets the address of the user's Facility.
     *
     * @param facilityAddress the address of the user's Facility.
     */
    public void setFacilityAddress(String facilityAddress) {
        this.facilityAddress = facilityAddress;
    }

    /**
     * Gets the user's facility description.
     *
     * @return Facility description of the user's Facility.
     */
    public String getFacilityDesc() {
        return facilityDesc;
    }

    /**
     * Sets the description of the user's Facility.
     *
     * @param facilityDesc the description of the user's Facility.
     */
    public void setFacilityDesc(String facilityDesc) {
        this.facilityDesc = facilityDesc;
    }

    /**
     * Gets the deviceId of the user.
     *
     * @return the deviceId of the user
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Sets the deviceId of the user.
     *
     * @param deviceID the deviceID of the user's device
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}