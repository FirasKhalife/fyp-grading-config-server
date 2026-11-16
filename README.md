# config-server

Serves the shared configuration of the **Assay** services from
[fyp-grading-config](https://github.com/FirasKhalife/fyp-grading-config). The services import it from `CONFIG_SERVER_URL` (it also registers with Eureka).

- **Profiles:**
  - `git` (the default) reads `GIT_REPO_URI` at `GIT_BRANCH`.
  - `native` reads a mounted folder (`CONFIG_DIR`). Use it to try an unpushed branch locally; the docker-compose
    repo mounts the local checkout this way.
- **Endpoints** live under `/config` (for example `/config/admin-service/default`), so they can't be mistaken for
  actuator paths.
- **Exposure:**
  - The service is on the internal network only; the gateway doesn't route it.
  - Its actuator exposes only health, info and prometheus.
  - The configuration it serves holds no secrets: the services take those from the environment.

Each service's own `application.yml` is complete, so the services also start without the config server.

## Run and test

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=native -Dspring-boot.run.arguments=--CONFIG_DIR=../fyp-grading-config
./mvnw test
```

The tests check that a service receives the shared configuration and that actuator stays closed.

Built on Spring Boot 4.0 and Spring Cloud 2025.1, Java 25, with a layered, non-root image.

## Lint and CI

- **Formatting:** Spotless with palantir-java-format. `./mvnw spotless:apply` fixes it.
- **Lint:** Checkstyle (`checkstyle.xml`) catches likely bugs and sloppy code.
- The build checks both in the `validate` phase, so `./mvnw verify` runs the lint, the build and every test.
- **GitHub Actions** (`.github/workflows/ci.yml`) runs on every pull request and on pushes to `master`:
  - the lint
  - `verify`, including the Testcontainers tests
  - a build of the Docker image
