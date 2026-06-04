# RedHat building microservices in quarkus DO378 notes

- https://quarkus.io/version/3.8/guides/

## Create application

```
mvn com.redhat.quarkus.platform:quarkus-maven-plugin:3.8.3.redhat-00003:create \
  -DprojectGroupId=com.redhat.smartcity \
  -DprojectArtifactId=parks \
  -Dextensions="hibernate-orm-panache,jdbc-postgresql,resteasy-reactive-jackson" \
  -DplatformVersion=3.8.3.redhat-00003
```

## Extensions
```
Database ORM          hibernate-orm-panache                 hibernate-reactive-panache
Database driver       jdbc-postgresql                       reactive-pg-client

REST                  resteasy-jackson                      resteasy-reactive-jackson
REST client           rest-client-jackson                   rest-client-reactive-jackson
OpenAPI               smallrye-openapi                      ditto

Messaging             smallrye-reactive-messaging-kafka     ditto

JWT                   smallrye-jwt-build                    ditto
                      smallrye-jwt                          ditto

SSO                   quarkus-oidc                          ditto
                      quarkus-keycload-authorization        ditto

OpenShift             quarkus-openshift                     ditto

Fault tolerance       smallrye-fault-tolerance              ditto

Health                smallrye-health                       ditto
```

### Imperative, Blocking
```
mvn quarkus:add-extension -Dextensions="quarkus-hibernate-orm-panache, jdbc-postgresql, resteasy, smallrye-openapi, smallrye-reactive-messaging-kafka"
```

### Reactive, Non-blocking
```
mvn quarkus:add-extension -Dextensions="quarkus-hibernate-reactive-panache, reactive-pg-client, resteasy-reactive, smallrye-openapi,smallrye-reactive-messaging-kafka"
```


## Dev services
```
quarkus.datasource.devservices.image-name=registry.ocp4.example.com:8443/redhattraining/do378-postgres:14.1
```

## Persistence - Panache Active Record Pattern

The Active Record pattern attaches the methods that access an entity to the entity itself, thus giving the entity an active role in its persistence. This reduces the number of external classes that manage the persistent objects, such as repositories or entity managers. One of the benefits of this approach is that objects can be used in the same way you use any other Java object, making development more intuitive, faster, and easier.

To use this pattern, extend your entities from PanacheEntity and use the methods directly on the entity instance, or the static methods inherited by your class.

### application.properties
```
quarkus.datasource.db-kind = postgresql
quarkus.datasource.username = ...
quarkus.datasource.password = ...
quarkus.datasource.jdbc.url = jdbc:postgresql://localhost:5432/mydatabase

quarkus.hibernate-orm.database.generation = drop-and-create
```

### code
```
@Entity
public class Speaker extends PanacheEntity {

	// public fields

	// public no-arg constructor
}

public class SomeResource {
    @GET
    @Path("/things-found-in/{city}")
    public List<Thing> thingsFoundInCity(
        @PathParam("city") String city,
        @QueryParam("page") @DefaultValue("0") int pageIndex,
        @QueryParam("size") @DefaultValue(20) int pageSize)
    {
        return Thing.find("city", Sort.by("name"), city)
                    .page(page, size)
                    .list();
    }
}
```

## REST - imperative, blocking

- https://quarkus.io/version/3.8/guides/rest-json
- https://quarkus.io/version/3.8/guides/openapi-swaggerui

### extensions

- smallrye-openapi exposes q/openapi and q/swagger-ui

```
REST                  resteasy-jackson                      resteasy-reactive-jackson
OpenAPI               smallrye-openapi
```

### code
```
@OpenAPIDefinition(
	info = @Info(
		title = "api title",
		version = "1.0.0"
	)
)

@Path("/endpoint")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Resource {

	@GET
	@Operation(
		operationId = "getSpeakerList", 
		description = "get list of speakers")
	@APIResponse(
		responseCode = "200",
		description = "a list of speakers",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation=Park.class, type=SchemaType.ARRAY)
        )
	)
    @APIResponse(
        responseCode = "204",
        description = "No parks found"
    )
	public List<Speaker> getSpeakerList() {
		...
		return List.of(...)
	}

	@POST
	@Path("/{speakerName}")
	@Transactional
	public Entity createSpeaker(@PathParam("speakerName") String speakerName) {

		Speaker s = new Speaker();
		s.name = speakerName;
	
		s.persist();

		return Response.created(...)
			.header("id", s.id)
			.build();
	}

	@POST
    @Transactional
	public Entity createSpeaker(@QueryParam("speakerName") @DefaultValue("unknown") String speakerName) {
		...
	}

    @PUT
    @Transactional
    public void update(Speaker incoming) {
        Speaker.<Speaker>findByIdOptional(incoming.id)
            .ifPresentOrElse(
                existing -> {
                    existing.name = incoming.name;
                    ...
                    existing.persist();
                },
                () -> {
                    throw new NotFoundException();
                }
            );
    }
}
```

## REST - reactive, non-blocking

- https://quarkus.io/version/3.8/guides/getting-started-reactive
- https://quarkus.io/version/3.8/guides/resteasy-reactive
- https://quarkus.io/version/3.8/guides/resteasy-reactive-migration
- https://quarkus.io/version/3.8/guides/quarkus-reactive-architecture

### extensions
```
REST                  ---                      resteasy-reactive-jackson
```

### code
```
@Path("/some-nonblocking-path")
public class SomeNonblockingResource {

    @GET
    ...
    public Uni<List<Thing>> list() {
        return Thing.listAll();
    }

    @POST
    ...
    @WithTransaction
    public Uni<Response> add(Thing newThing) {
        return Thing.<Thing>findById(newThing.id)
            .onItem()
                .ifNotNull()
                .failWith(() ->
                    new WebApplicationException(
                        "Thing already exists with id=" + newThing.id,
                        Response.Status.CONFLICT
                    )
                )
            .onItem()
                .ifNull()
                .continueWith(newThing)
                .call(thing -> thing.persist())
                .replaceWith(
                    Response.status(Response.Status.CREATED)
                        .entity(newThing)
                        .build()
                );
    }

    @PUT
    ...
    @WithTransaction
    public Uni<Response> update(Thing incoming) {
        return Thing.<Thing>findById(incoming.id)
            .onItem()
                .ifNull()
                    .failWith(NotFoundException::new)
                .invoke(existing -> {
                    existing.name = incoming.name;
                    ...
                })
                .call(existing -> existing.persist())
                .replaceWith(Response.noContent().build());
    }
}
```


## REST client - reactive and non-reactive

- https://quarkus.io/version/3.8/guides/rest-client-reactive

### extensions

- reactive extension handles both reactive and non-reactive code

```
REST client           -na-                   rest-client-reactive-jackson
```


### application.properties
```
quarkus.rest-client.weather-client.url=http://localhost:8090
```

### code - imperative, blocking
```
@Path("/warnings")
@RegisterRestClient(configKey = "weather-client")
public interface WeatherClient {
    @GET
    @Path("/{city})
    public List<WeatherWarning> getWarningsByCity(@PathParam("city") String city);
    ...
}

public class WeatherService {

	@RestClient
	WeatherClient blockingWeatherClient;
	...

    void weatherWarningsByCity(String city) { // no return value
        var warningList = blockingWeatherClient.getWarningsByCity(city)
        for(WeatherWarning warning : warningList) {
            ...
        }
    }
}
```

### code - reactive, non-blocking
```
@Path("/warnings")
@RegisterRestClient(configKey = "weather-client")
public interface WeatherClient {
    @GET
    @Path("/{city})
    public Uni<List<WeatherWarning>> ... // Uni<List<WeatherWarning> is a stream
                                         // containing a single List<WeatherWarning>
    ...
}

public class WeatherService {

	@RestClient
	WeatherClient nonblockingWeatherClient;
	...

    Uni<Void> weatherWarningsByCity(String city) { // must return Uni<Void>
        return nonblockingWeatherClient.getWarningsByCity(city)
            .onItem()
            .invoke(
                warningList -> {
                    for(WeatherWarning warning : warningList) {
                        ...
                    }
                }
            )
            .replaceWithVoid();
    }
}
```

## Unit testing

- if the DO378 test harness, ```lab grade comprehensive-review```, reports no Surefire reports,
  then run ```mvn test```. Quarkus bypass Maven Surefire which is why we have no Surefire reports when we run ```mvn quarkus:dev``` or ```mvn quarkus:test```

- https://quarkus.io/version/3.8/guides/getting-started-testing
- https://github.com/rest-assured/rest-assured
- https://quarkus.io/version/3.8/guides/tests-with-coverage

```
@QuarkusTest
class ParksResourceTest {

	@Test
	void getParksShouldReturn4Parks() {
		given()
			.when().get("/parks")
			.then()
				.statusCode(200)
                .body(not(emptyArray()))
				.body("$.size()", is(4))
                .body("[0].name", is(...));
	}
}
```

## Integration testing

- See section 16 at https://quarkus.io/version/3.8/guides/getting-started-testing

```
@QuarkusIntegrationTest
class ParksResourceIT extends ParksResourceTest {
    // TODO
}
```

## Reactive Messaging - Event Driven Architecture

- https://quarkus.io/version/3.8/guides/kafka-reactive-getting-started
- https://smallrye.io/smallrye-reactive-messaging/4.16.0/

### extensions
```
Reactive messaging      smallrye-reactive-messaging
                        smallrye-reactive-messaging-kafka
```

### notes

- one channel binds/maps to exactly one topic
- a channel is the MicroProfile Reactive Messaging name
- a topic is the external kafka concept

### application properties
```
kafka.bootstrap.servers = localhost:9092

# Incoming Channel
mp.messaging.incoming.an-incoming-channel.connector = smallrye-kafka
mp.messaging.incoming.an-incoming-channel.topic = an-incoming-topic
mp.messaging.incoming.an-incoming-channel.auto.offset.reset = earliest
mp.messaging.incoming.an-incoming-channel.value.deserializer = my.package.serde.SomeEventDeserializer

# Outgoing Channel
mp.messaging.outgoing.an-outgoing-channel.connector = smallrye-kafka
mp.messaging.outgoing.an-outgoing-channel.topic = an-outgoing-topic
mp.messaging.outgoing.an-outgoing-channel.value.serializer = io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

### code
```
public record SomeEvent(String eventName, int eventSize) {}

public class SomeEventDeserializer extends ObjectMapperDeserializer<SomeEvent> {
    public SomeEventDeserializer() {
        super(BankAccountWasCreated.class);
    }
}

public class SomeResource {
    
    @Channel("some-channel-out")
    Emitter<SomeEvent> emitter;

    @POST
    @Transactional
    public void create(Thing thing) {
        thing.persist();
        emitter.send(new SomeEvent("created a thing", 1));
    }
}

@ApplicationScoped
public class SomeEventProcessor {

    @Incoming("some-channel-in")
    public Uni<Void> processSomeEvent(SomeEvent event) {
        // do something with event
        return Uni.createFrom().voidItem();
    }
}

@ApplicationScoped
public class StatusProducer {

    @Outgoing("status-channel")
    public Multi<StatusEvent> broadcastStatus() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(15))
            .map(tick -> new StatusEvent("healthy", Instant.now()));
    }
}
```


## Securing - TLS, CORS

- https://quarkus.io/version/3.8/guides/security-cors
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CORS

### application.properties
```
quarkus.http.cors=true
%dev.quarkus.http.cors.origins=http://localhost:9000
%dev.quarkus.http.cors.methods=GET,POST,PUT
```

## Securing - Authentication, Authorisation

- https://quarkus.io/version/3.8/guides/security-jwt

### extensions
```
JWT (consuming)       smallrye-jwt
JWT (producing)       smallrye-jwt-build
```

### application.properties
- we have copied public PEM files to ```src/main/resources```
```
# if we are consuming JWT
mp.jwt.verify.issuer=parks-service
mp.jwt.verify.publickey.location=publicKey.pem

# if we are signing (producing) a JWT
smallrye.jwt.sign.key.location=privateKey.pem

# authorisation via properties
quarkus.security.jaxrs.deny-unannotated-endpoints=true

quarkus.http.auth.policy1.roles-allowed=maintainer,admin
quarkus.http.auth.permission.permission1.policy=policy1
quarkus.http.auth.permission.permission1.paths=/secured/*
quarkus.http.auth.permission.permission1.methods=DELETE

# with built-in policies - permit (PermitAll) and deny (DenyAll)
quarkus.http.auth.permission.permission2.policy=permit
quarkus.http.auth.permission.permission2.paths=/public/*
```

### code
```
@Path("/whoami")
public class WithSecurityIdentityResource {

	@Inject
	SecurityIdentity identity;

	@GET
	public String whoAmI() {
		return identity.getPrincipal();
	}
}

@Path("/whoami")
public class WithSecurityContextResource {

	@GET
	public String whoAmI(@Context SecurityContext ctx) {
		return ctx.getUserPrincipal().getName();
	}
}

public class Resource {

	@GET
	@PermitAll  // allow authenticated *and* unauthenticated users
	public List<Speaker> getSpeakerList() {
		...
	}

	@DELETE
	@RolesAllowed( {"Maintainer", "Admin"} ) // allow authenticated user with one of these roles
	public void deleteSpeaker(Speaker speaker) {
		...
	}
}
```

## Securing - JWT

Often we obtain a JWT from an identity manager. If we want
to generate our own JWT, we can use this.

- https://quarkus.io/version/3.8/guides/security-jwt-build

### extensions
```
JWT build             quarkus-smallrye-jwt-build
```

### application.properties
we have copied private PEM files to ```src/main/resources```
```
mp.jwt.verify.issuer=https://example.com/issuer
mp.jwt.verify.publickey.location=publicKey.pem
smallrye.jwt.sign.key.location=privateKey.pem
```

### code
```
@ApplicationScoped
public class JwtGenerator {

	public String generateToken(String username) {
		return Jwt.issuer("https://example.com/issuer")
			.upn(username)
			.groups(Set.of("User", "Admin"))
			.claim(Claim.somekey, "some value")
			.claim("a_key", "a_value")
			.sign();
	}
}

public class Service {

	@Inject
	JwtGenerator generator;

	public String generateToken(String username) {
		return generator.generateToken(username);
	}
}

```


## Logging

- https://quarkus.io/version/3.8/guides/logging#logging-format

### Extensions
```
Logging               (built-in jboss logging)
                      quarkus-logging-json
```

### application.properties
```
# logging
quarkus.log.level=INFO
quarkus.log.category."com.redhat.training.expense".level=DEBUG
%dev.quarkus.log.file.enable=true
%dev.quarkus.log.file.path=/home/student/DO378/monitor-logging/dev.logs
%dev.quarkus.log.file.format=%d %-5p [%F] %m%n
%dev.quarkus.log.file.rotation.rotate-on-boot=false
%dev.quarkus.log.console.json=false
%dev.quarkus.log.console.json.pretty-print=true
```

### code
```
...
public class Resource {

	@GET
	public List<Speaker> getSpeakerList() {
		Log.debug(message)
		...
	}
}
```


## Health

- https://quarkus.io/version/3.8/guides/smallrye-health

### Extensions

- exposes q/health

```
mvn quarkus:add-extension -Dextensions="smallrye-health"
```

### code

```
@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {
	@Override
	public HealthCheckResponse call() {
		return HealthCheckResponse.up("readiness-check");
	}
}

@Startup
...
public class StartupCheck implements HealthCheck {
	@Override
	public HealthCheckResponse call() {
		return HealthCheckResponse.named("startup-check")
				.up()
				.withData("key", "value")
				.build();
	}
}

@Liveness
...
public class Liveness implements HealthCheck {
	...
}
```

## Metrics

- https://quarkus.io/version/3.8/guides/telemetry-micrometer

### Extensions

- exposes q/health in prometheus format

```
mvn quarkus:add-extension -Dextensions="quarkus-micrometer, quarkus-micrometer-registy-prometheus"
```

### application.properties
```
# metrics

```

### code
```
...
public class Resource {

	@Inject
	public MeterRegistry registry;

	private final StopWatch stopWatch = StopWatch.createStarted();

	@PostConstruct
	public void initMeters() {
		registry.guage(
			"my.gauge.metric.name",
			"Tags.of("key1", "value1"),
			stopWatch,
			StopWatch::getTime
		);
	}

	@GET
	public List<Speaker> getSpeakerList() {
		stopWatch.reset();
		stopWatch.start();
		...
	}

	@GET
	public List<Speaker> getSpeakerList() {
		registry.counter("calls.to.get.speaker").increment();
		...
	}

	@GET
	@Counted("calls.to.get.speaker")
	public List<Speaker> getSpeakerList() {
		...
	}

	@POST
	public Speaker create(Speaker speaker) {
		return registry.timer("speaker.creation.time")
			.wrap(
				(Supplier<Speaker>) () -> service.createSpeaker(speaker)
			).get();
	}
}
```

## Open telemetry

- https://quarkus.io/version/3.8/guides/opentelemetry

### Extensions
```
Open telemetry        quarkus-opentelemetry
```

### application.properties
```
# open telemetry
quarkus.otel.service.name=myservice
quarkus.otel.exporter.otlp.traces.endpoint=http://localhost:4317
quarkus.otel.traces.sampler=traceidratio
quarkus.otel.traces.sampler.arg=0.5
quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, spanId=%X{spanId}, parentId=%X{parentId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n
```

### code
```
...
public class Resource {

	@GET
	// extension creates a default span for each service call
	public List<Speaker> getSpeakerList() {
		...
	}

	@GET
	@WithSpan
	public List<Speaker> getSpeakerList() {
		...
	}

	@POST
	@AddingSpanAttributes
	public Speaker create(@SpanAttribute("speaker") Speaker speaker) {
		...
	}
}
```

## Fault tolerance

- https://quarkus.io/version/3.8/guides/smallrye-fault-tolerance

### Extensions
```
mvn quarkus:add-extension -Dextensions="smallrye-fault-tolerance"
```

### code
```
class SomeResource {

    @GET
    @Retry( maxRetries = 2, retryOn = SomeException.class)
    ...

    @GET
    @Timeout( 3000 )
    public List<Thing> list() {
        try {
            return Thing.listAll();
        } catch (TimeoutException e) {
            // do something on timeout
        }
    }

    @GET
    @CircuitBreaker( requestVolumeThreshold = 4 )
    @Fallback( fallbackMethod = "myFallbackMethod" )
    public List<Thing> listByName(String name) {
        ...
    }

    public List<Thing> myFallbackMethod(String name) {
        // return a canned list of Thing
    }
}
```