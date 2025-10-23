package be.codesolutions.pwm.boundary;

import be.codesolutions.pwm.entity.PwmOutput;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Black-box system tests for PWM hardware integration.
 * <p>
 * These tests verify the complete PWM control flow on actual Raspberry Pi hardware,
 * including Pi4J initialization, device creation, and REST API control.
 * <p>
 * Tests are disabled by default and must be explicitly enabled with:
 * {@code pwm.system-test.enabled=true}
 * <p>
 * Usage:
 * <pre>
 * # On development machine (tests skipped)
 * mvn clean test
 *
 * # On Raspberry Pi (tests execute)
 * mvn clean test -Dpwm.system-test.enabled=true
 * </pre>
 */
@QuarkusTest
class PwmSystemTest {

    @Inject
    @RestClient
    PwmResourceClient client;

    @ConfigProperty(name = "pwm.system-test.enabled", defaultValue = "false")
    boolean systemTestEnabled;

    @ConfigProperty(name = "pwm.system-test.output.id")
    String testOutputId;

    @ConfigProperty(name = "pwm.system-test.output.pin")
    int testOutputPin;

    @ConfigProperty(name = "base_uri/mp-rest/url")
    String baseUri;

    /**
     * Verifies that the PWM service properly initializes all hardware outputs
     * and exposes them via the REST API with correct configuration.
     */
    @Test
    void shouldInitializeHardware() {
        // Skip if system tests are disabled
        assumeTrue(systemTestEnabled, "System tests disabled - set pwm.system-test.enabled=true to run on Raspberry Pi");

        // Verify service initialized all outputs
        List<PwmOutput> outputs = client.getAllOutputs();
        assertThat(outputs)
                .as("All configured PWM outputs should be initialized")
                .hasSize(5);

        // Verify test output exists with the correct pin
        PwmOutput testOutput = client.getOutput(testOutputId);
        assertThat(testOutput.id())
                .as("Test output ID should match configuration")
                .isEqualTo(testOutputId);
        assertThat(testOutput.pin())
                .as("Test output GPIO pin should match configuration")
                .isEqualTo(testOutputPin);

        System.out.printf("Successfully verified hardware initialization (baseUri: %s)%n", baseUri);
    }

    /**
     * Verifies that PWM values can be controlled via the REST API and that
     * the service correctly maintains and reports the current state.
     */
    @Test
    void shouldControlPwmOutput() {
        assumeTrue(systemTestEnabled, "System tests disabled - set pwm.system-test.enabled=true to run on Raspberry Pi");

        // Set PWM value to 50%
        PwmOutput result = client.setPwmValue(testOutputId, 0.5);

        // Verify immediate response
        assertThat(result.id())
                .as("Response should contain the correct output ID")
                .isEqualTo(testOutputId);
        assertThat(result.value())
                .as("Response should reflect the new PWM value")
                .isEqualTo(0.5);

        // Verify persisted state
        PwmOutput current = client.getOutput(testOutputId);
        assertThat(current.value())
                .as("PWM value should persist after being set")
                .isEqualTo(0.5);

        System.out.printf("Successfully controlled PWM output %s: set to 0.5%n", testOutputId);

        // Reset to 0 for clean state
        client.setPwmValue(testOutputId, 0.0);
        System.out.printf("Reset PWM output %s to 0.0%n", testOutputId);
    }
}