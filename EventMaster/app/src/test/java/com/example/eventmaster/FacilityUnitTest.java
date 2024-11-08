package com.example.eventmaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test class for the Facility class.
 */
public class FacilityUnitTest {
    /**
     * Tests the getter for the facility name.
     */
    @Test
    void testGetFacilityName() {
        Facility userFacility = new Facility("TestIDUnitTest", "Empty Facility", "5676 12th Ave", "There is nothing here");
        assertEquals("Empty Facility", userFacility.getFacilityName());
    }

    /**
     * Tests the setter for the facility name.
     */
    @Test
    void testSetFacilityName() {
        Facility userFacility = new Facility("TestIDUnitTest", "Empty Facility", "5676 12th Ave", "");
        userFacility.setFacilityName("Updated Empty Facility");
        assertEquals("Updated Empty Facility", userFacility.getFacilityName());
    }

    /**
     * Tests the getter for the device ID.
     */
    @Test
    void testGetDeviceID() {
        Facility userFacility = new Facility("TestIDUnitTest", "Empty Facility", "5676 12th Ave", "There is nothing here");
        assertEquals("TestIDUnitTest", userFacility.getDeviceID());
    }

    /**
     * Tests the setter for the facility description.
     */
    @Test
    void testSetFacilityDescription() {
        Facility userFacility = new Facility("TestIDUnitTest", "Empty Facility", "5676 12th Ave", "");
        userFacility.setFacilityDesc("Updated Description");
        assertEquals("Updated Description", userFacility.getFacilityDesc());
    }

}
