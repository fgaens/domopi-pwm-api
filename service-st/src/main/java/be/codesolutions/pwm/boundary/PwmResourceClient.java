package be.codesolutions.pwm.boundary;

import be.codesolutions.pwm.entity.PwmOutput;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * REST client for the PWM Resource API.
 * Provides a type-safe client interface for system tests to interact with the PWM service.
 */
@RegisterRestClient(configKey = "base_uri")
@Path("/pwm")
public interface PwmResourceClient {

    /**
     * Get all PWM outputs configured in the system.
     *
     * @return list of all PWM outputs with their current state
     */
    @GET
    List<PwmOutput> getAllOutputs();

    /**
     * Get a specific PWM output by ID.
     *
     * @param id the output identifier
     * @return the PWM output with current state
     */
    @GET
    @Path("/{id}")
    PwmOutput getOutput(@PathParam("id") String id);

    /**
     * Set the PWM value for a specific output.
     *
     * @param id    the output identifier
     * @param value the PWM duty cycle (0.0 - 1.0)
     * @return the updated PWM output
     */
    @PUT
    @Path("/{id}")
    PwmOutput setPwmValue(@PathParam("id") String id, @QueryParam("value") double value);
}