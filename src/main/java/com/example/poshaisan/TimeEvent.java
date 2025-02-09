package com.example.poshaisan;

import java.time.LocalDateTime;

/**
 * Enum representing the type of time event.
 */
enum TimeEventType {
    ARRIVAL,
    DEPARTURE
}


/**
 * Represents a time event with a type and timestamp.
 */
public record TimeEvent(LocalDateTime time,
                        TimeEventType type) implements Comparable<TimeEvent> {

    // Methods -----------------------------------------------------

    /**
     * Compares this TimeEvent with another TimeEvent based on the time.
     *
     * @param other the other TimeEvent to compare to.
     * @return a negative integer, zero, or a positive integer as this
     * TimeEvent is earlier than,
     * equal to, or later than the specified TimeEvent.
     */
    @Override
    public int compareTo(TimeEvent other) {
        return this.time.compareTo(other.time);
    }
}
