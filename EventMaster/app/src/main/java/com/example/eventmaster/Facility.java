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

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityAddress() {
        return facilityAddress;
    }

    public void setFacilityAddress(String facilityAddress) {
        this.facilityAddress = facilityAddress;
    }

    public String getFacilityDesc() {
        return facilityDesc;
    }

    public void setFacilityDesc(String facilityDesc) {
        this.facilityDesc = facilityDesc;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}