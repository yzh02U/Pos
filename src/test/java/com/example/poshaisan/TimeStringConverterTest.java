package com.example.poshaisan;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TimeStringConverterTest {

    private final TimeStringConverter controller = new TimeStringConverter();

    @Test
    public void testToString() {
        assertThat(controller.toString(0), equalTo("00:00"));
        assertThat(controller.toString(1), equalTo("00:01"));
        assertThat(controller.toString(60), equalTo("01:00"));
        assertThat(controller.toString(61), equalTo("01:01"));
        assertThat(controller.toString(150), equalTo("02:30"));
        assertThat(controller.toString(600), equalTo("10:00"));
        assertThat(controller.toString(1439), equalTo("23:59"));
    }

    @Test
    public void testFromString() {
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> controller.fromString("10:00")
        );

        assertThat(exception.getMessage(),
                   is("Not needed for this implementation"));
        assertThat(exception, instanceOf(UnsupportedOperationException.class));
    }
}
