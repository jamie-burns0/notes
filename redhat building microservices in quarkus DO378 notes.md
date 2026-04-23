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
Database ORM          quarkus-hibernate-orm-panache         quarkus-hibernate-reactive-panache
Database driver       jdbc-postgresql                       reactive-pg-client

REST                  resteasy-jackson                      resteasy-reactive-jackson
REST client           quarkus-rest-client-jackson           quarkus-rest-client-reactive-jackson
OpenAPI               smallrye-openapi                      ditto

Messaging             smallrye-reactive-messaging-kafka     ditto

JWT                   smallrye-jwt-build                    ditto
                      smallrye-jwt                          ditto

SSO                   quarkus-oidc                           ditto
                      quarkus-keycload-authorization         ditto

OpenShift             quarkus-openshift                      ditto

Fault tolerance       smallrye-fault-tolerance               ditto

Health                smallrye-health                        ditto
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

## Active Record Pattern
The Active Record pattern attaches the methods that access an entity to the entity itself, thus giving the entity an active role in its persistence. This reduces the number of external classes that manage the persistent objects, such as repositories or entity managers. One of the benefits of this approach is that objects can be used in the same way you use any other Java object, making development more intuitive, faster, and easier.

To use this pattern, extend your entities from PanacheEntity and use the methods directly on the entity instance, or the static methods inherited by your class.

### code
```
@Entity
public class Speaker extends PanacheEntity {

	// public fields

	// public no-arg constructor
}
```

## REST

- https://quarkus.io/version/3.8/guides/rest-json

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
		description = "returns an list of speakers"
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
	public Entity createSpeaker(@QueryParam("speakerName") @DefaultValue("unknown") String speakerName) {
		...
	}
}
```

## REST client

- https://quarkus.io/version/3.8/guides/rest-client-reactive

### application.properties
```
quarkus.rest-client.weather-service.url=http://localhost:8090
```

### code
```
@Path("/warnings")
@RegisterRestClient(configKey = "weather-service")
public interface WeatherClient {
	...
}

public class WeatherService {

	@RestClient
	WeatherClient weatherClient;

	...
}
```

## Testing

- if the DO378 test harness, ```lab grade comprehensive-review```, reports no Surefire reports,
  then run ```mvn test```. Quarkus bypass Maven Surefire which is why we have no Surefire reports when we run ```mvn quarkus:dev``` or ```mvn quarkus:test```

```
@QuarkusTest
public ParksResourceTest {

	@Test
	void getParksShouldReturn4Parks() {
		given()
			.when().get("/parks")
			.then()
				.statusCode(200)
				.body("$.size()", is(4));
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

## Securing - TLS, CORS

- https://quarkus.io/version/3.8/guides/security-cors
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CORS

### application.properties
```
quarkus.http.cors=true
%dev.quarkus.http.cors.origins=http://localhost:9999
%dev.quarkus.http.cors.methods=GET,POST
```

## Securing - Authentication, Authorisation

- https://quarkus.io/version/3.8/guides/security-jwt

### extensions
```
JWT                   quarkus-smallrye-jwt
```

### application.properties
- we have copied public PEM files to ```src/main/resources```
```
mp.jwt.verify.issuer=https://example.com/issuer
mp.jwt.verify.publickey.location=publicKey.pem

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
smallrye.jwt.sign.key.location=privateKey.pem
```

### code
```
@ApplicationScoped
public class JwtGenerator {

	public String generateToken(String username) {
		return Jwt.issuer("http://example.com/issuer")
			.upn(username)
			.groups(Set.of("Admin"))
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


## Health

- https://quarkus.io/version/3.8/guides/smallrye-health

### Extensions
```
Health                quarkus-smallrye-health
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
```
Metrics               quarkus-micrometer
                      quarkus-micrometer-registry-prometheus
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

