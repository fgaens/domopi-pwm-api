package be.codesolutions.qmp.greetings.control;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static java.lang.System.Logger.Level.INFO;

@ApplicationScoped
public class Greeter {

    static final System.Logger LOG = System.getLogger(Greeter.class.getName());

    private final String message;

    public Greeter(@ConfigProperty(defaultValue = "hello, Quarkus on BCE", name = "message") String message) {
        this.message = message;
    }

    public String greetings() {
        LOG.log(INFO, "returning: " + this.message);
        return this.message;
    }

    public void greetings(String userMessage) {
        LOG.log(INFO, "received: " + userMessage);
    }
}
