Health: smallrye-health
Fault tolerance: smallrye-fault-tolerance
Metrics: quarkus-microprofile, quarkus-microprofile-registry-prometheus
Opentelemetry: quarkus-opentelemetry

## Health

@Readiness
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
            .up()
            .withData("key", "value")
            .build();
    }
}

## Fault tolerance

public class SomeResource {
    @GET
    @Retry(maxRetries=4, retryOn=SomeException.class)
    @Timeout(3000)
    @Fallback(fallbackMethod="someFallback")
    public List<Thing> list() {
        try {
            return ...
        } catch(TimeoutException e) {
            // do something
        }
    }

    public List<Thing> someFallback() {
        return ... // canned list of Thing
    }

    @GET
    @CircuitBreaker(requestVolumeThreshold = 4)
    ...
}

## Metrics

public class SomeResource {
    @Inject
    MeterRegistry registry;

    @GET
    public List<Thing> countedList() {
        registry.counter("countedList.call.counter").increment();
        ...        
    }

    @GET
    @Counter("another.call.counter")
    public List<Thing> anotherList() {
        ...
    }

    @GET
    public List<Thing> timedList() {
        return registry.timer("timedList.timer")
            .wrap(
                (Supplier<List<Thing>>) () -> {
                    return ... // get our list of Thing
                }
            )
            .get();
    }

    // Guage: TODO
}

## Open telemetry

public class SomeResource {
    @GET
    @WithSpan("my-span-name")
    ...

    @GET
    @WithSpanAttributes
    public List<Thing> listThingsOverAge(@SpanAttribute("thingAge") String thingAge) {
        ...
    }
}