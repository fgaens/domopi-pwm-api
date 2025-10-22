package be.codesolutions.qmp.greetings.boundary;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class GreetingsResourceIT {

    @Inject
    @RestClient
    GreetingsResourceClient rut;

    @Inject
    @ConfigProperty(name = "base_uri/mp-rest/url")
    String baseURI;


    @Test
    void hello() {
        var message = this.rut.content();
        assertNotNull(message);
        System.out.printf("baseURI: %s message: %s%n", baseURI, message);
    }

}
