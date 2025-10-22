package be.codesolutions.pwm.entity;

/**
 * Represents a single PWM output device.
 * This entity encapsulates the configuration and current state of a PWM-controlled output.
 *
 * @param id    unique identifier for the output (e.g., "zk", "mb")
 * @param pin   GPIO pin number
 * @param value current PWM value (0.0 - 1.0)
 */
public record PwmOutput(String id, int pin, double value) {

    public PwmOutput {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("PWM value must be between 0.0 and 1.0, got: " + value);
        }
    }

    public PwmOutput(String id, int pin) {
        this(id, pin, 0.0);
    }

    public PwmOutput withValue(double newValue) {
        return new PwmOutput(id, pin, newValue);
    }
}