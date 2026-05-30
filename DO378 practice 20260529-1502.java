mvn com.redhat.quarkus.platform:quarkus-maven-plugin:version:create \
-DprojectGroupId=com.redhat.smartcity \
-DprojectArtifactId=parks \
-DplatformVersion=version \
-Dextensions="hibernate-orm-panache,jdbc-postgresql,resteasy-reactive-jackson"
-Dextensions="hibernate-reactive-panache,reactive-pg-client,resteasy-reactive-jackson"

quarkus.datasource.devservices.image-name=...

## Security - CORS

quarkus.http.cors=true
%dev.quarkus.http.cors.origins=...
%dev.quarkus.http.cors.methods=...

## REST - blocking + OpenApi

mvn quarkus:add-extension -Dextensions="smallrye-openapi"

- exposed q/openapi, q/swagger-ui

@Path("/some-path")
public class SomeResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "listAllThings",
        description = "list all Things",
    )
    @APIResponse(
        responseCode = "200",
        description = "a list of all Things",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(
                implementation = Thing.class,
                type = SchemaType.ARRAY
            )
        )
    )
    public List<Thing> list() {
        return Thing.listAll();
    }

    @POST
    ...
    @APIResponse(
        responseCode = "201",
        description = "thing created"
    )
    @APIResponse(
        responseCode = "409",
        description = "thing already exists"
    )
    @Transactional
    public Thing add(Thing newThing) {
        Thing.<Thing>findByIdOptional(newThing.id)
            .ifPresent(existing -> {
                throw new WebApplicationException(
                    "Thing already exists with id=" + newThing.id,
                    Response.Status.CONFLICT
                );
            });

        newThing.persist();
        return newThing;
    }

    @PUT
    ...
    @APIResponse(
        responseCode = "204",
        description = "thing updated"
    )
    @APIResponse(
        responseCode = "404",
        description = "thing not found"
    )
    @Transactional
    public Response update(Thing incoming) {
        Thing existing = Thing.<Thing>findByIdOptional(incoming.id)
            .orElseThrow(NotFoundException::new);

        existing.name = incoming.name;
        ...
        existing.persist();

        return Response.noContent().build();
    }
}

## REST - non-blocking

mvn quarkus:add-extension -Dextensions="resteasy-reactive-jackson"

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

## Unit test

@QuarkusTest
public class SomeResourceTest {
    @Test
    void getReturnsANonEmptyList() {
        given()
        .when()
            .get("/some-path")
        .then()
            .statusCode("200")
            .body(not(emptyArray()))
            .body("$.size()", is(4))
            .body("[0].name", is(...));
    }
}

## REST client - blocking and non-blocking

mvn quarkus:add-extension -Dextensions="rest-client-reactive-jackson"

quarkus.rest-client.some-service.url=http://${WEBSERVICE_HOST:localhost}:${WEBSERVICE_PORT:8090}

@ApplicationScoped
@RegisterRestClient(configKey = "some-service")
public interface SomeServiceBlockingRestClient {
    @GET
    @PATH("/some-service-blocking-path/{id}")
    public ServiceThing get(@PathParam("id") String id);
}

@ApplicationScoped
@RegisterRestClient(configKey = "some-service")
public interface SomeServiceNonblockingRestClient {
    @GET
    @PATH("/some-service-nonblocking-path/{id}")
    public Uni<ServiceThing> get(@PathParam("id") String id);
}

public class SomeOtherService {

    @RestClient
    SomeServiceBlockingRestClient blockingClient;

    void workWithServiceThing(String id) {
        var serviceThing = blockingClient.get(id);
        // do something with serviceThing
    }

    @RestClient
    SomeServiceNonblockingRestClient nonblockingClient;

    Uni<Void> nonblockingWorkWithServiceThing(String id) {
        return nonblockingClient.get(id)
            .onItem()
                .invoke(
                    serviceThing -> {
                        // do something with serviceThing
                    }
                )
                .replaceWithVoid();
    }

    Uni<Void> nonblockingWorkWithServiceThingUsingCall(String id) {
        return nonblockingClient.get(id)
            .onItem()
                .call(serviceThing -> {
                    // do something reactive with serviceThing
                    return someReactiveStep(serviceThing);
                })
                .replaceWithVoid();
    }
}

## Reactive messaging - event driven architecture

mvn quarkus:add-extension -Dextensions="smallrye-reactive-messaging-kafka"

kafka.bootstrap.servers=...

# map microprofile reative messaging channel to a kafka topic

mp.messaging.incoming.an-incoming-channel.connector = smallrye-kafka
mp.messaging.incoming.an-incoming-channel.topic = an-incoming-topic
mp.messaging.incoming.an-incoming-channel.auto.offset.reset = earliest
mp.messaging.incoming.an-incoming-channel.value.deserialize = my.package.serde.SomeEventDeserializer

mp.messaging.outgoing.an-outgoing-channel.connector = smallrye-kafka
mp.messaging.outgoing.an-outgoing-channel.topic = an-outgoing-topic
mp.messaging.outgoing.an-outgoing-channel.value.serializer = io.quarkus.kafka.client.serialization.ObjectMapperSerializer

public record SomeEvent(...) {}

public class SomeEventDeserializer extends ObjectMapperDeserializer<SomeEvent> {
    public SomeEventDeserializer() {
        super(SomeEvent.class);
    }
}

public class SomeResource {
    @Channel("an-outgoing-channel")
    Emitter<SomeEvent> emitter;
    ...
    public void update(Thing incoming) {
        // update our existing thing
        emitter.send(new SomeEvent(...));
    }
}

@ApplicationScoped
public class SomeEventProcessor {
    @Incoming("an-incoming-channel")
    public void workWithThing(Thing thing) {
        // work with thing
    }
}

@ApplicationScoped
public class SomeEventProducer {
    @Outgoing("an-outgoing-channel")
    publc Multi<SomeEvent> streamSomeEvent() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(15))
            .map(tick -> new SomeEvent(...));
    }
}


## Health

mvn quarkus:add-extension -Dextensions="smallrye-health"

- exposes q/health

@Readiness
@ApplicationScoped
public ReadinessCheck extends HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("readiness-check");
    }
}

@Startup
...
public StartupCheck extends HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("startup-check")
            .up()
            .data("some-key", "some-value")
            .build();
    }
}

@Liveness
...


## Metrics

mvn quarkus:add-extension -Dextensions="quarkus-micrometer, quarkus-micrometer-registry-prometheus"

- exposes q/metrics in prometheus format

class SomeResource {
    @Inject
    public MeterRegistry registry;

    public void someMethod(...) {
        // do something we want to measure
        registry.counter("my-counter").increment();
    }
}


## Security - produce JWT

mvn quarkus:add-extension -Dextensions="smallrye-jwt-build"

smallrye.jwt.sign.key.location = some-private.pem

public class JwtSupport {
    public String generateToken(String username) {
        return Jwt.issuer("some-issuer")
            .upn(username)
            .groups(Set.of("User","Admin"))
            .claim(Claim.claimKey, "value1")
            .claim("key2", "value2")
            .sign();
    }
}


## Security - consume JWT

mvn quarkus:add-extension -Dextensions="smallrye-jwt"

mp.jwt.verify.issuer = some-issuer
mp.jwt.verify.publickey.location = some-public.pem


## Security - authentication and authorisation

class SomeResource {
    @PUT
    ...
    @AllowRoles({"User","Admin"})
    public void update(Thing thing) {
        ...
    }
}

