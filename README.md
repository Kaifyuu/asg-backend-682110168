# asg-backend-682110168

Backend Development individual assignment — a Spring Boot / Spring Data JPA REST API built on top of the provided `sample-boot-modules` multi-module skeleton (`domain-model`, `web-service`, `web-front`).

The system revolves around **generating random data points and comparing them**: you create a `Dataset` with a `GeneratorConfig` (min/max range, sample count, optional seed), the server randomly fills it with `DataPoint`s, and you can then run a `Comparison` between any two data points to see which is greater and by how much.

## How to run

```
mvn spring-boot:run -pl web-service -am
```

The app uses an in-memory **H2** database (see `web-service/src/main/resources/application.properties`), seeded on startup from `web-service/src/main/resources/data.sql`. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:spring_db`).

To run the tests:

```
mvn test -pl web-service -am
```

## Domain model

Four entities live in the `domain-model` module (`th.mfu.domain`), demonstrating all three required JPA relationship types:

```
Dataset  1 ────── 1 GeneratorConfig      (One-to-One)
Dataset  1 ────── * DataPoint            (One-to-Many / Many-to-One)
Comparison * ───── 1 DataPoint (pointA)  (Many-to-One)
Comparison * ───── 1 DataPoint (pointB)  (Many-to-One)
```

- **Dataset** — a named collection of data points (`name`, `description`, `createdAt`). Owns one `GeneratorConfig` and contains many `DataPoint`s.
- **GeneratorConfig** — the settings used to randomly generate a dataset's points (`minValue`, `maxValue`, `sampleCount`, optional `seed` for reproducible runs). Holds the foreign key back to `Dataset` (`dataset_id`, unique), making it the owning side of the **one-to-one** relationship.
- **DataPoint** — a single numeric value (`value`, `position`, `generatedAt`), either randomly generated or manually entered. Belongs to exactly one `Dataset` (**many-to-one**).
- **Comparison** — the recorded result of comparing two `DataPoint`s (`result`: `A_GREATER`/`B_GREATER`/`EQUAL`, `difference`, `comparedAt`). References `pointA` and `pointB`, each a **many-to-one** to `DataPoint`.

Each entity has a matching DTO in `web-service/.../dto` (`DatasetDTO`, `GeneratorConfigDTO`, `DataPointDTO`, `ComparisonDTO`) so that JPA entities (with their lazy associations) are never serialized directly over the REST API — this avoids infinite-recursion/lazy-loading issues and keeps the API contract decoupled from the persistence model.

## Random generation

`POST /api/datasets` takes a `name`/`description` plus a `generatorConfig` (`minValue`, `maxValue`, `sampleCount`, optional `seed`). The service uses `java.util.Random` (seeded if provided, otherwise randomly seeded) to fill the new dataset with `sampleCount` values uniformly distributed in `[minValue, maxValue)`, all persisted in a single transaction via cascading save. `POST /api/datasets/{id}/regenerate` re-rolls a fresh random sample for an existing dataset using its stored config. Individual points can also be entered manually via `POST /api/data-points`.

## REST API

Each of the three controllers (`DatasetController`, `DataPointController`, `ComparisonController`) exposes the full CRUD verb set required by the assignment:

| Verb                | Dataset                    | DataPoint                   | Comparison                  |
|----------------------|-----------------------------|-------------------------------|-------------------------------|
| Create (`POST`)      | `/api/datasets`             | `/api/data-points`            | `/api/comparisons`            |
| List (`GET`)         | `/api/datasets`             | `/api/data-points`            | `/api/comparisons`            |
| Update (`PATCH`)     | `/api/datasets/{id}`        | `/api/data-points/{id}`       | `/api/comparisons/{id}`       |
| Delete (`DELETE`)    | `/api/datasets/{id}`        | `/api/data-points/{id}`       | `/api/comparisons/{id}`       |

`PATCH` endpoints apply a partial update: only non-null fields present in the request body are changed. `POST /api/comparisons` accepts `pointAId`/`pointBId`; the server looks up both `DataPoint`s and computes `result`/`difference` server-side (never trusted from client input).

## Tests

`web-service/src/test/java/th/camt/controller` contains `MockMvc`-based integration tests (`@SpringBootTest` + `@AutoConfigureMockMvc`), one test class per controller, each covering all four REST verbs (Create / List / Update / Delete):

- `DatasetControllerTest` — also verifies random generation actually produces `sampleCount` points
- `DataPointControllerTest`
- `ComparisonControllerTest` — also exercises the two Many-to-One relationships to `DataPoint` and the server-computed `result`/`difference`

Tests run against their own isolated in-memory H2 database and each test method runs inside a rolled-back transaction (`@Transactional`), so they don't depend on `data.sql` and don't interfere with each other.

## DataDuel game

`web-service/src/main/resources/static/index.html` is a static, vanilla-JS single-page app served automatically by Spring Boot at `http://localhost:8080/` (no build step, calls the REST API above via `fetch`). It's a higher/lower/equal card game built on top of the `Dataset`/`DataPoint` API:

- Draws a deck of `DataPoint`s and you guess whether the next card is higher, lower, or equal to the current one (keybinds: `↑`/`W` higher, `↓`/`S` lower, `E` equal, `Space`/`Enter`/`→` next card).
- Scoring rewards accuracy and late-game risk: score scales with cards left in the deck and grows exponentially (`ROUND_BASE ^ decksCleared`) the more decks you clear.
- Each card tracks both its position within the current deck and its all-time position across every deck ever dealt, so returning cards from earlier decks stay comparable.
- Visual feedback (French-card-style corners, screen shake, particle bursts, a rising "heat" glow) ramps up as the stakes climb.

## Project structure

- `domain-model` — JPA entities (`th.mfu.domain`)
- `web-service` — Spring Boot application: repositories, DTOs, services, REST controllers, tests (`th.camt`)
- `web-front` — separate JAX-RS/Jetty front-end module (unchanged, provided by the starter skeleton)

## AI usage disclosure

Claude (Anthropic, Sonnet 5, via Cowork) was used to:

- Analyze the original e-commerce domain model in this repo and redesign it around a new theme (random data point generation and comparison) per the assignment brief and the student's direction.
- Design the domain model (entity fields and the One-to-One / Many-to-One / One-to-Many relationships).
- Generate the JPA entities, Spring Data repositories, DTOs, service layer (including the `java.util.Random`-based generation logic), and REST controllers (Create/List/Patch/Delete) in `domain-model` and `web-service`.
- Rewrite `data.sql` to match the new schema.
- Write the MockMvc integration tests in `web-service/src/test/java`.
- Build the DataDuel game frontend (`web-service/src/main/resources/static/index.html`) on top of the REST API.
- Fix a foreign-key violation in `DatasetService.regenerate()` (comparisons referencing a dataset's points now get deleted before the points do).
- Rewrite this README.

All generated code was reviewed manually for correctness. Automated `mvn test` execution could not be fully verified in the assistant's sandboxed environment (no network access to Maven Central to download dependencies), so tests should be (and were intended to be) verified locally with `mvn test` before submission. I can explain any part of this code in person as required.
