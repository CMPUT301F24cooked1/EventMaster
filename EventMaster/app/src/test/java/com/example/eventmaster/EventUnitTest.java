package com.example.eventmaster;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * Runs unit tests for the Event Class.

public class EventUnitTest {
    /**
     * Tests the getter for the Event name.

    @Test
    void testGetEventName() {
        long date = 1748761200;
        Date testDate = new Date(date);
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        assertEquals("Hanatarash Live Show", userEvent.getEventName());
    }

    /**
     * Tests the setter for the Event name.

    @Test
    void testSetEventName() {
        long date = 1748761200;
        Date testDate = new Date(date);
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        userEvent.setEventName("Boredoms Live Show");
        assertEquals("Boredoms Live Show", userEvent.getEventName());
    }

    /**
     * Tests the getter for the Event capacity.

    @Test
    void testGetEventCapacity() {
        long date = 1748761200;
        Date testDate = new Date(date);
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        assertEquals(100, userEvent.getEventCapacity());
    }

    /**
     * Tests the setter for the Event capacity.

    @Test
    void testSetEventCapacity() {
        long date = 1748761200;
        Date testDate = new Date(date);
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        userEvent.setEventCapacity(0);
        assertEquals(0, userEvent.getEventCapacity());
    }

    /**
     * Tests the getter for the final Event date.

    @Test
    void testGetDate() {
        long date = 1748761200;
        Date testDate = new Date(date);
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        assertEquals(testDate, userEvent.getEventFinalDate());
    }

    /**
     * Tests the setter for the final Event date.

    @Test
    void testSetDate() {
        long date = 1748761200;
        Date testDate = new Date(date);
        long date2 = 1748764800;
        Date testDate2 = new Date(date2);
        Event userEvent = new Event("Hanatarash Live Show", "Fun time at a music live show. Nothing to worry about. Have a good time.", "TestIDUnitTest", 100, 500, testDate, true);
        userEvent.setEventFinalDate(testDate2);
        assertEquals(testDate2, userEvent.getEventFinalDate());
    }
}
/**/