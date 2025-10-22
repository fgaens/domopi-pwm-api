package be.codesolutions.pwm.boundary;

import be.codesolutions.pwm.control.PwmOutputController;
import be.codesolutions.pwm.entity.PwmOutput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * REST API boundary for PWM output control.
 * Provides endpoints to query and control PWM outputs.
 */
@Path("pwm")
@ApplicationScoped
public class PwmResource {

    private final PwmOutputController controller;

    public PwmResource(PwmOutputController controller) {
        this.controller = controller;
    }

    @GET
    public List<PwmOutput> getAllOutputs() {
        return controller.getAllOutputs();
    }

    @GET
    @Path("{id}")
    public Response getOutput(@PathParam("id") String id) {
        return controller.getOutput(id)
                .map(output -> Response.ok(output).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("{id}")
    public Response setPwmValue(
            @PathParam("id") String id,
            @QueryParam("value") Double value) {

        if (value == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required query parameter: value")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        try {
            PwmOutput updatedOutput = controller.setPwmValue(id, value);
            return Response.ok(updatedOutput).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }
}