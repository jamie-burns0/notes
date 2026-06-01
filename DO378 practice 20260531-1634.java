Readiness:       "smallrye-health"
Fault tolerance: "smallrye-fault-tolerance"
Metrics:         "quarkus-micrometer, quarkus-micrometer-registry-prometheus"
Opentelemetry:   "quarkus-opentelemetry"


## Health - fault tolerance

mvn quarkus:add-extension -Dextensions="smallrye-fault-tolerance"

public class SomeResource {

    @GET
    @Retry(maxRetries = 4, retryOn = SomeException.class)
    @Fallback(fallbackMethod = "someFallback")
    public List<Thing> list() {
        ...
    }

    public List<Thing> someFallback() {
        // return a canned List of Thing
    }

    @GET
    @Timeout(3000)
    public List<Thing> listOldThings() {
        try {
            // return a list of old things
        } catch( TimeoutException e) {
            // do something on timeout
        }
    }
    @GET
    @CircuitBreaker(requestVolumeThreshold = 4)
    ...
}

## Health - metrics

mvn quarkus:add-extension -Dextensions="quarkus-micrometer, quarkus-micrometer-registry-prometheus"

public class SomeResource {
    @Inject
    MetricRegistry registry;

    @GET
    public List<Thing> list() {
        ...
        registry.counter("calls.to.get").increment();
    }

    @POST
    public Thing update(Thing newThing) {
        registry.timer("my.add.timer")
            .wrap(
                (Supplier<Thing>) () -> {
                    // add new Thing
                    // return added Thing
                }
            )
            .get()
    }

    // Guage: TODO
}

## Health - readiness

mvn quarkus:add-extension -Dexceptions="smallrye-health"

@Ready
@ApplicationScoped
public class ReadyCheck extends HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("ready-check");
    }
}

@Startup
@ApplicationScoped
public class StartupCheck extends HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("startup-check")
            .up()
            .build();
    }
}

@Liveness
@ApplicationScoped
public class LivenessCheck extends HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.builder()
            .name("liveness-check")
            .withData("some-key", "some-value")
            .up()
            .build();
    }
}


## Health - opentelemetry

mvn quarkus:add-extension -Dextensions="quarkus-opentelemetry"

public class SomeResource {
    @GET
    @WithSpan("my-get-span")
    ...

    @GET
    @AddingSpanAttributes
    public void someMethod(@SpanAttribute("someArg") String someArg) {
        ...
    }

}