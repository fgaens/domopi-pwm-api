package be.codesolutions.pwm.control;

import be.codesolutions.pwm.entity.PwmOutput;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Control layer for managing PWM outputs using Pi4J.
 * Handles hardware interaction and business logic for PWM devices.
 */
@ApplicationScoped
public class PwmOutputController {

    static final System.Logger LOG = System.getLogger(PwmOutputController.class.getName());
    private static final int PWM_FREQUENCY = 1500;

    private final boolean enableHardware;
    private final Map<String, Integer> outputPins;
    private final Map<String, PwmOutput> outputs;
    private final Map<String, Pwm> pwmDevices;
    private Context pi4j;

    public PwmOutputController(
            @ConfigProperty(name = "pwm.outputs.zk.pin") int zkPin,
            @ConfigProperty(name = "pwm.outputs.mb.pin") int mbPin,
            @ConfigProperty(name = "pwm.outputs.lk.pin") int lkPin,
            @ConfigProperty(name = "pwm.outputs.kk.pin") int kkPin,
            @ConfigProperty(name = "pwm.outputs.bk.pin") int bkPin,
            @ConfigProperty(name = "pwm.hardware.enabled", defaultValue = "true") boolean enableHardware) {

        this.enableHardware = enableHardware;
        this.outputPins = Map.of(
                "zk", zkPin,
                "mb", mbPin,
                "lk", lkPin,
                "kk", kkPin,
                "bk", bkPin
        );
        this.outputs = new HashMap<>();
        this.pwmDevices = new HashMap<>();
    }

    @PostConstruct
    void initialize() {
        LOG.log(INFO, "Initializing PWM output controller (hardware enabled: {0})", enableHardware);

        // Always initialize the outputs map (needed for business logic)
        for (Map.Entry<String, Integer> entry : outputPins.entrySet()) {
            String id = entry.getKey();
            int pin = entry.getValue();
            PwmOutput output = new PwmOutput(id, pin);
            outputs.put(id, output);
        }

        // Only initialize Pi4J hardware if enabled
        if (!enableHardware) {
            LOG.log(INFO, "Hardware disabled - running in test mode");
            return;
        }

        try {
            this.pi4j = Pi4J.newAutoContext();

            for (Map.Entry<String, Integer> entry : outputPins.entrySet()) {
                String id = entry.getKey();
                int pin = entry.getValue();

                PwmConfig config = Pwm.newConfigBuilder(pi4j)
                        .id(id)
                        .name("PWM-" + id)
                        .address(pin)
                        .pwmType(PwmType.SOFTWARE)
                        .frequency(PWM_FREQUENCY)
                        .dutyCycle(0)
                        .build();

                Pwm pwm = pi4j.create(config);
                pwmDevices.put(id, pwm);

                LOG.log(INFO, "Initialized PWM output: {0} on pin {1}", id, pin);
            }
        } catch (Exception e) {
            LOG.log(WARNING, "Failed to initialize Pi4J context: {0}", e.getMessage());
        }
    }

    @PreDestroy
    void shutdown() {
        LOG.log(INFO, "Shutting down PWM output controller");

        // Turn off all outputs
        outputs.keySet().forEach(id -> {
            try {
                setPwmValue(id, 0.0);
            } catch (Exception e) {
                LOG.log(WARNING, "Failed to turn off output {0}: {1}", id, e);
            }
        });

        // Shutdown Pi4J context
        if (pi4j != null) {
            try {
                pi4j.shutdown();
            } catch (Exception e) {
                LOG.log(WARNING, "Error during Pi4J shutdown: {0}", e);
            }
        }
    }

    public List<PwmOutput> getAllOutputs() {
        return List.copyOf(outputs.values());
    }

    public Optional<PwmOutput> getOutput(String id) {
        return Optional.ofNullable(outputs.get(id));
    }

    public PwmOutput setPwmValue(String id, double value) {
        PwmOutput output = outputs.get(id);
        if (output == null) {
            throw new IllegalArgumentException("Unknown output ID: " + id);
        }

        PwmOutput updatedOutput = output.withValue(value);
        outputs.put(id, updatedOutput);

        // Only interact with hardware if enabled
        if (enableHardware) {
            Pwm pwm = pwmDevices.get(id);
            if (pwm != null) {
                // Convert 0.0-1.0 to duty cycle percentage (0-100)
                float dutyCycle = (float) (value * 100.0);
                pwm.on(dutyCycle);
                LOG.log(INFO, "Set PWM output {0} to {1} (duty cycle: {2}%)", id, value, dutyCycle);
            }
        }

        return updatedOutput;
    }
}