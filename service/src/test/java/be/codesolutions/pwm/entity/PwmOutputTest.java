package be.codesolutions.pwm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PwmOutputTest {

    @Test
    void shouldRejectValueBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> new PwmOutput("test", 13, -0.1));
    }

    @Test
    void shouldRejectValueAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> new PwmOutput("test", 13, 1.1));
    }

    @Test
    void shouldAcceptValidValues() {
        assertDoesNotThrow(() -> new PwmOutput("test", 13, 0.0));
        assertDoesNotThrow(() -> new PwmOutput("test", 13, 0.5));
        assertDoesNotThrow(() -> new PwmOutput("test", 13, 1.0));
    }

    @Test
    void shouldValidateOnWithValue() {
        PwmOutput output = new PwmOutput("test", 13, 0.5);
        assertThrows(IllegalArgumentException.class, () -> output.withValue(1.5));
    }
}