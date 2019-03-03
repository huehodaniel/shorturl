# ShortURL

URL shortener project developed in Kotlin, using Spring Boot.
Includes small frontend and backend.

## How to run

### Local

By default, ShortURL will execute in local mode, using ephemeral persistence with H2
and binding to port 8080.
Build the project with Gradle (`gradlew build`) and
execute it with `java -jar build/libs/shorturl-0.0.2-SNAPSHOT.jar`.

### Docker Swarm

To execute ShortURL in a local Docker cluster with load balancing and persistence with PostgreSQL,
ensure your machine is a manager node with `docker swarm init` and execute `docker stack deploy -c docker-compose.yml shorturl`.

### Demo

A cloud-deployed demo is available in https://shorturl.hueho.xyz, using DigitalOcean infrastructure.

#### Self-service

Deployment instructions for cloud environments aren't available yet, but you can either
use the Docker configuration in a cloud-exposed Swarm manager node, or deploy the jar
and execute it with `java -jar shorturl-0.0.2-SNAPSHOT.jar --spring.profiles.active=cloud`,
overriding the relevant connection settings in a `application.properties` in the same folder:

- `spring.datasource.url`: a JDBC formatted-URL for the database
- `spring.datasource.username`: DB username
- `spring.datasource.password`: DB password

Notice that cloud deployment implies use of the PostgreSQL database.

## Endpoints

### Version 0.1

- `/`: ShortURL front end.
    - `GET /`: front page, with minimal UI for shortening URLs.
    - `GET /ga/{hashed id}`
        - if the given id exists, redirect immediately with a `302 Found`.
        - if the id doesn't exist, redirect to root.
- `/api`: REST API endpoint.
    - `GET /`: returns fixed sanity JSON with API version.
        - Response:
            - `name`: fixed, `"ShortURL"`
            - `version`: currently `0.1`
            - example:
            ```
            {
                "name": "ShortURL",
                "version": "0.1"
            }
            ```
    - `GET /{hashed id}`: returns information about shortened URL with given id.
        - Response:
            - `id`: hashed id
            - `short`: short URL pointing to the frontend endpoint
            - `long`: long, original URL
            - `visits`: how many times was the URL visited
            - example:
            ```
            {
                "id": "XD1E51z7",
                "short": "http://localhost:8080/ga/XD1E51z7",
                "long": "https://getbootstrap.com/docs/4.3/content/reboot/#html5-hidden-attribute",
                "visits": 3
            }
            ```
    - `POST /`: saves a new URL in the system, or if the URL already exists, returns the existing information for it
        - Request:
            - Content type: `text/plain`
            - Body: the URL to be shortened
        - Response:
            - `id`: hashed id
            - `short`: short URL pointing to the frontend endpoint
            - `long`: long, original URL
            - `visits`: how many times was the URL visited
            - example:
            ```
            {
                "id": "XD1E51z7",
                "short": "http://localhost:8080/ga/XD1E51z7",
                "long": "https://getbootstrap.com/docs/4.3/content/reboot/#html5-hidden-attribute",
                "visits": 3
            }
            ```

## Architecture

The codebase uses a simple, layered architecture using the Spring Framework, following
best pratices of Domain-Driven Design.
It does not group classes and code by layer, but instead by business context,
better explained [here](https://hexdocs.pm/phoenix/contexts.html).

Given the simplicity of the application, that mostly means separating the code in two contexts:
- `urls`, with code related to storing and manipulating the URLs themselves
- `frontend`, with code related to accessing the application

The use of Kotlin makes it easier to organize relevant code and keeping them close without exploding the
number of packages or source files.

The persistence layer uses Spring JDBCTemplate for manipulating data, for complexity reasons - mostly
because the project is not a full CRUD, so it's easier to wire up the raw queries and use the lightweight
facilites of JDBCTemplate than using full JPA/Hibernate entities.

The controller/presentation layer uses Spring WebMVC. For the REST API, model-specific controller,
the `@RestController` annotation for better semantics and using automatic serialization was used.
For the frontend controller, plain `@Controller` was used for easier control (the frontend controller
only handles redirects - the frontpage is served through Spring Boot support for static pages).

For generating hashed IDs for the short URLs, the [Hashids library](https://hashids.org/) was used.