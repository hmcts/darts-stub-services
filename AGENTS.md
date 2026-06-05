You are an expert in Java 21, Spring Boot services, API stubbing, and reliable test support tooling. You write maintainable server-side code and keep local and container-based developer workflows straightforward.

## Repo Context
- DARTS Stub Services is a Gradle-based Spring Boot application
- The service is typically built with the Gradle wrapper and run via Docker Compose
- The application exposes a health endpoint on port `4551` when running locally in Docker
- The repository includes support assets such as Postman collections, infrastructure files, and WireMock resources

## Java And Spring Guidance
- Target Java 21 and keep code compatible with the configured Gradle toolchain
- Prefer small focused Spring components with clear responsibilities
- Preserve actuator health behaviour and local startup simplicity
- Avoid introducing secrets or environment-specific values into source control

## Build And Test
- Use `./gradlew build` for a full build
- Use `./gradlew assemble` to prepare the distribution artifacts
- Keep the documented Docker flow working:
  - `docker-compose build`
  - `docker-compose up`
- The documented shortcut for local Docker startup is `./bin/run-in-docker.sh`
- If you change ports, startup behaviour, or required variables, update `README.md`

## Testing Structure
- The build defines unit, integration, functional, and smoke test source sets
- Keep new tests in the appropriate source set rather than overloading `src/test`
- Preserve Gradle task names and verification behaviour because CI may depend on them

## Stub And Support Assets
- Treat `wiremock/`, Postman collections, and container scripts as part of the developer contract
- Keep ARM, DAR, and test WireMock mappings aligned with the behaviour expected by consumers
- If a code change requires updates to stubs or sample requests, keep those assets aligned in the same change
- Prefer deterministic stub behaviour over hidden magic or environment-specific branching

## Operational Safety
- Keep local developer workflows simple and reproducible
- Do not assume global Gradle installation; use the wrapper
- Avoid destructive Docker cleanup guidance in code changes unless explicitly required

## Review Guidelines
- Prioritise findings that would break local Docker startup, health checks, Gradle verification tasks, or stub compatibility
- Flag undocumented config changes, port changes, or changes that make the service harder to run locally
- Prefer clear API stub behaviour and predictable test fixtures
