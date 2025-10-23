# TODO

## Add System Tests for Hardware Integration

### Overview
Add black-box system tests in the `service-st` module to verify the complete PWM control flow on actual Raspberry Pi hardware.

### Goal
Test the full hardware initialization and control flow:
1. Service startup with Pi4J initialization
2. PWM device creation for configured outputs
3. Setting PWM values via REST API
4. Verifying responses match the expected output state

### Approach

**Configuration-based testing**-Similar to the integration tests, use a flag to enable/skip system tests:
- `pwm.system-test.enabled=false` (default - skips hardware tests)
- `pwm.system-test.enabled=true` (runs on actual Pi hardware)

**Single output testing**-Configure one test output to avoid interference:
- `pwm.system-test.output.id=zk`
- `pwm.system-test.output.pin=13`

### Implementation Steps

#### 1. Add System Test Configuration
Create `service-st/src/test/resources/application.properties`:
```properties
# System test configuration (requires actual Raspberry Pi)
pwm.system-test.enabled=false
pwm.system-test.output.id=zk
pwm.system-test.output.pin=13

# Base URI for system tests
base_uri/mp-rest/url=${BASE_URI:http://localhost:8080}
```

#### 2. Create a REST Client
Create `service-st/src/main/java/be/codesolutions/pwm/boundary/PwmResourceClient.java`:
```java
@RegisterRestClient(configKey = "base_uri")
@Path("/pwm")
public interface PwmResourceClient {

    @GET
    List<PwmOutput> getAllOutputs();

    @GET
    @Path("/{id}")
    PwmOutput getOutput(@PathParam("id") String id);

    @PUT
    @Path("/{id}")
    PwmOutput setPwmValue(@PathParam("id") String id, @QueryParam("value") double value);
}
```

#### 3. Create System Test
Create `service-st/src/test/java/be/codesolutions/pwm/boundary/PwmSystemTest.java`:
```java
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

    @Test
    void shouldInitializeHardware() {
        // Skip if system tests are disabled
        assumeTrue(systemTestEnabled, "System tests disabled");

        // Verify service initialized all outputs
        List<PwmOutput> outputs = client.getAllOutputs();
        assertThat(outputs).hasSize(5);

        // Verify test output exists with the correct pin
        PwmOutput testOutput = client.getOutput(testOutputId);
        assertThat(testOutput.id()).isEqualTo(testOutputId);
        assertThat(testOutput.pin()).isEqualTo(testOutputPin);
    }

    @Test
    void shouldControlPwmOutput() {
        assumeTrue(systemTestEnabled, "System tests disabled");

        // Set PWM value
        PwmOutput result = client.setPwmValue(testOutputId, 0.5);

        // Verify response
        assertThat(result.id()).isEqualTo(testOutputId);
        assertThat(result.value()).isEqualTo(0.5);

        // Verify persisted state
        PwmOutput current = client.getOutput(testOutputId);
        assertThat(current.value()).isEqualTo(0.5);

        // Reset to 0
        client.setPwmValue(testOutputId, 0.0);
    }
}
```

#### 4. Update service-st/pom.xml
Add required dependencies:
```xml
<dependencies>
    ...
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest-client</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-rest-client-jackson</artifactId>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
    ...
</dependencies>
```

### Running System Tests

#### On Development Machine (tests skipped)
```bash
cd service-st
mvn clean test
# Tests will be skipped due to assumeTrue(systemTestEnabled)
```

#### On Raspberry Pi (tests execute)
```bash
# Ensure service is running
cd ../service
mvn quarkus:dev

# In another terminal, run system tests
cd ../service-st
export BASE_URI=http://localhost:8080
mvn clean test -Dpwm.system-test.enabled=true
```

### Notes

- System tests require the service to be running on actual Raspberry Pi hardware
- Tests are safe to run and won't interfere with production usage
- The test output should be connected to a load that can safely handle PWM signals
- Tests use `assumeTrue()` to skip gracefully when hardware is not available
- Default configuration has tests disabled to prevent failures on non-Pi environments

### Related Files

- Service module: `service/src/main/java/be/codesolutions/pwm/`
- Integration tests: `service/src/test/java/be/codesolutions/pwm/boundary/PwmResourceTest.java`
- Current system test module: `service-st/` (needs implementation)