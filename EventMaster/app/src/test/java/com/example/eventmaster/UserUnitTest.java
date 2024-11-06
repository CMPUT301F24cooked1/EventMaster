package com.example.eventmaster;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the User class.
 */
public class UserUnitTest {
    /**
     * Tests the getter for the name.
     */
    @Test
    void testGetName() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        assertTrue(user.getName().equals("Name"));

    }
    /**
     * Tests the setter for the name.
     */
    @Test
    void testSetName() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        user.setName("NewName");
        assertTrue(user.getName().equals("NewName"));

    }
    /**
     * Tests the getter for the device ID.
     */
    @Test
    void testGetDeviceID() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        assertTrue(user.getDeviceId().equals("123456"));

    }
    /**
     * Tests the setter for the device ID.
     */
    @Test
    public void testSetDeviceId() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        user.setDeviceId("2468");
        assertEquals("2468", user.getDeviceId());
    }
    /**
     * Tests the getter for the email.
     */
    @Test
    void testGetEmail() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        assertTrue(user.getEmail().equals("email@gmail.com"));

    }
    /**
     * Tests the setter for the email.
     */
    @Test
    void testSetEmail() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        user.setName("newemail@gmail.com");
        assertTrue(user.getName().equals("newemail@gmail.com"));

    }
    /**
     * Tests the getter for the phone number.
     */
    @Test
    void testPhoneNumber() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        assertTrue(user.getPhone_number().equals("7801234567"));

    }
    /**
     * Tests the setter for the phone number.
     */
    @Test
    void testGetPhoneNumber() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        user.setName("7803750274");
        assertTrue(user.getName().equals("7803750274"));

    }
    /**
     * Tests the setter/getter for the phone notifications.
     */
    @Test
    public void testSetNotificationsEnabled() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        user.setNotifications(true);
        assertTrue(user.getNotifications());
    }
    /**
     * Tests the setter/getter for the phone notifications.
     */
    @Test
    public void testSetNotificationsDisabled() {
        Profile user = new Profile("123456", "Name", "email@gmail.com", "7801234567");
        user.setNotifications(false);
        assertFalse(user.getNotifications());
    }



}
