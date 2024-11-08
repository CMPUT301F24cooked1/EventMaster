package com.example.eventmaster;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class EventUnitTest {
    /**
     * Tests the getter for the Event name.
     */
    @Test
    void testGetEventName() {
        Date testDate = new Date("2025-06-01T07:00:00");
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        assertEquals("Hanatarash Live Show", userEvent.getEventName());
    }

    /**
     * Tests the setter for the Event name.
     */
    @Test
    void testSetEventName() {
        Date testDate = new Date("2025-06-01T07:00:00");
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        userEvent.setEventName("Boredoms Live Show");
        assertEquals("Boredoms Live Show", userEvent.getEventName());
    }

    /**
     * Tests the getter for the Event capacity.
     */
    @Test
    void testGetEventCapacity() {
        Date testDate = new Date("2025-06-01T07:00:00");
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        assertEquals(100, userEvent.getEventCapacity());
    }

    /**
     * Tests the setter for the Event capacity.
     */
    @Test
    void testSetEventCapacity() {
        Date testDate = new Date("2025-06-01T07:00:00");
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        userEvent.setEventCapacity(0);
        assertEquals(0, userEvent.getEventCapacity());
    }

    /**
     * Tests the getter for the final Event date.
     */
    @Test
    void testGetDate() {
        Date testDate = new Date("2025-06-01T07:00:00");
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        assertEquals(testDate, userEvent.getEventFinalDate());
    }

    /**
     * Tests the setter for the final Event date.
     */
    @Test
    void testSetDate() {
        Date testDate = new Date("2025-06-01T07:00:00");
        Date testDate2 = new Date("2005-06-01T07:00:00");
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        userEvent.setEventFinalDate(testDate2);
        assertEquals(testDate2, userEvent.getEventFinalDate());
    }
}
