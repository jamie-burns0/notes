mvn com.redhat.quarkus.platform:quarkus-maven-plugin:version:create \
-DprojectGroupId=com.redhat.smartcity \
-DprojectArtifactId=parks \
-DplatformVersion=version \
-Dextensions="hibernate-orm-panache,jdbc-postgresql,resteasy-reactive-jackson"

quarkus.datasource.devservices.image-name=...

# Security - CORS

quarkus.http.cors=true
%dev.quarkus.http.cors.origins=...
%dev.quarkus.http.cors.methods=...

# REST - blocking + OpenApi

mvn quarkus:add-extension -Dextensions="smallrye-openapi"

@Path("/some-path")
class SomeResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "list whatever",
        description = "get a list of whatever"
    )
    @APIResponse(
        responseCode = "200",
        description = "a list of whatever"
    )
    @APIResponse(
        responseCode = "204",
        description = "no whatevers found",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation=Whatever.class, type=SchemaType.ARRAY)
        )
    )
    public List<Whatever> list() {
        return Whatever.listAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void add(Whatever newWhatever) {
        Whatever.<Whatever>findByIdOptional(newWhatever.id)
            .isPresent(
                // we have a duplicate - throw an exception
            );
        newWhatever.persist();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Whatever update(Whatever incoming) {
        return Whatever.<Whatever>findByIdOptional(incoming.id)
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

# REST - non-blocking

- Uni returns a stream with a 0..1 objects
- Multi returns a stream with 0..* objects

@Path("/nonblocking-path")
class SomeResource {
    @GET
    ...
    public Uni<List<Whatever>> list() {
        return TODO
    }

    @PUT
    ...
    public Uni<Void> update(Whatever incoming) {
        return TODO
    }
}

# Unit test

@QuarkusTest
class SomeResourceTest {
    @Test
    void getReturnsANonEmptyList() {
        given()
        .when()
            .get("/some-path")
        .then()
            .statusCode(200)
            .body(not(emptyArray()))
            .body("$.size()", is(...))
            .body("[0].name", is(...));
    }
}

# Health

mvn quarkus:add-extension -Dextensions="smallrye-health"

- exposes q/health

@Readiness
@ApplicationScoped
class ReadinessCheck extends HealthCheck {
    @Override
    HealthResponseCheck call() {
        return HealthResponseCheck.up("readiness-check");
    }
}

# REST client - blocking

mvn quarkus:add-extension -Dextensions="rest-client-jackson"

quarkus.rest-client.some-client.url=http://${SERVICE_HOST:localhost}:${SERVICE_PORT:8090}

@ApplicationScoped
@RegisterRestClient(configKey = "some-client")
interface SomeClient {
    @Path("/whatever/{id}")
    Whatever getWhatever(@PathParam("id") String id);
}


public class SomeResource {
    @RestClient
    SomeClient client;
    ...
    Whatever getWhateverFromClient(String id) {
        return client.getWhatever(id);
    }
}

# REST client - non-blocking

mvn quarkus:add-extension -Dextensions="rest-client-reactive-jackson"

@ApplicationScoped
@RegisterRestClient(configKey = "some-client")
interface SomeClient {
    @GET
    @Path("/whatever/{id}")
    Uni<Whatever> getWhatever(@PathParam("id") String id);

    PUT
    @Path("/...")
    Uni<Void> saveWhatever(Whatever whatever);
}


public class SomeResource {
    @RestClient
    SomeClient client;
    ...
    Uni<Void> doSomething(String id) {
        client.getWhatever(id)
            .onItem()
            .invoke(
                whatever -> {
                    // do something with whatever
                }
            )
            .replaceWithVoid();
    }

    Uni<Void> saveWhatever(Whatever whatever) {
        return client.saveWhatever();
    }
}

# Security - consume JWT

mvn quarkus:add-extension -Dextensions="smallrye-jwt"

mp.jwt.verify.issuer=...
mp.jwt.verify.publickey.location=...

# Security - produce JWT

mvn quarkus:add-extension -Dextensions="smallrye-jwt-build"

smallrye.jwt.sign.key.location=...

class JwtService {
    String generateToken(String username) {
        return Jwt.issuer("some-service")
            .upn(username)
            .groups(Set.of("User","Admin"))
            .claim(Claim.somekey, "some-value"..)
            .claim("claim-key", "claim-value")
            .sign()
    }
}
