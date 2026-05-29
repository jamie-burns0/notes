mvn com.redhat.quarkus.platform:quarkus-maven-plugin:version:create \
-DprojectGroupId=com.redhat.smartcity \
-DprojectArtifactId=parks \
-DplatformVersion=version \
-Dextensions="hibernate-orm-panache,jdbc-postgresql,resteasy-reactive-jackson"
-Dextensions="hibernate-reactive-panache,reactive-pg-client,resteasy-reactive-jackson"

quarkus.datasource.devservices.image-name=...

# Security - CORS

quarkus.http.cors=true
%dev.quarkus.http.cors.origins=...
%dev.quarkus.http.cors.methods=...

# REST - blocking + OpenApi

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
}

# Unit test

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