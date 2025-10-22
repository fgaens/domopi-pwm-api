/**
 * REST resources and their corresponding MicroProfile REST client interfaces for system testing.
 * MicroProfile REST client interfaces for system testing.
 * <p>
 * The REST client interfaces reside in the same package as the resources under test to maintain
 * clear correspondence and simplify testing setup. This co-location makes it explicit which client
 * tests which resource and reduces navigation overhead during test development.
 * This package contains only test-independent client code. The client interfaces mirror
 * the service module's package structure to maintain clear correspondence with the resources
 * under test and simplify testing setup.
 * <p>
 * All MicroProfile REST client interfaces in this source directory (src/main/java) are packaged into a jar and can be
 * reused as clients by other services.
 * Client interfaces are named after their corresponding resources with the "Client" suffix.
 * <p>
 * All MicroProfile REST client interfaces in this source directory (src/main/java) are packaged
 * into a jar and can be reused as clients by other services.
 */
package be.codesolutions.qmp.greetings.boundary;
