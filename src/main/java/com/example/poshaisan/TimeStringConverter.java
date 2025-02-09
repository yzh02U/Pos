package com.example.poshaisan;

import javafx.util.StringConverter;

/**
 * A StringConverter for converting time in minutes to a formatted string
 * (HH:mm).
 */
public class TimeStringConverter extends StringConverter<Number> {

    // Methods -----------------------------------------------------

    /**
     * Converts a Number representing total minutes to a formatted time
     * string (HH:mm).
     *
     * @param object the Number to convert.
     * @return the formatted time string.
     */
    @Override
    public String toString(Number object) {
        int totalMinutes = object.intValue();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    /**
     * Throws UnsupportedOperationException as this method is not needed for
     * this implementation.
     *
     * @param string the string to convert.
     * @return nothing.
     * @throws UnsupportedOperationException always.
     */
    @Override
    public Number fromString(String string) {
        throw new UnsupportedOperationException(
                "Not needed for this implementation");
    }
}
