package be.codesolutions.qmp.greetings.boundary;

import be.codesolutions.qmp.greetings.control.Greeter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("hello")
@ApplicationScoped
public class GreetingResource {

    private final Greeter greeter;

    public GreetingResource(Greeter greeter) {
        this.greeter = greeter;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return this.greeter.greetings();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void hello(String message) {
        this.greeter.greetings(message);
    }
}